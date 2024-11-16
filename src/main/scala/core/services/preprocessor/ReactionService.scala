package core.services.preprocessor

import cats.effect.Concurrent
import cats.implicits._
import core.domain.preprocessor.{Reaction, ReactionDetails, ReactionId}
import core.errors.http.preprocessor.ReactionError
import core.errors.http.preprocessor.ReactionError._
import core.services.cache.CacheService
import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}
import io.circe.syntax._
import io.circe.parser.decode
import org.http4s.circe._

class ReactionService[F[_]: Concurrent](
  client:       Client[F],
  cacheService: CacheService[F],
  baseUri:      Uri
) {

  def getReaction(id: ReactionId): F[ReactionDetails] =
    cacheService.getReaction(id).flatMap {
      case Some(cachedReaction) => cachedReaction.pure[F]
      case None                 => fetchReactionFromRemote(id)
    }

  def createReaction(reaction: Reaction): F[Reaction] =
    makeRequest[Reaction](
      Request[F](Method.POST, baseUri).withEntity(reaction.asJson),
      responseBody =>
        decode[Reaction](responseBody).leftMap { error =>
          CreationError(s"Failed to create Reaction: ${error.getMessage}")
        }
    ).flatTap(createdReaction =>
      cacheService.putReaction(createdReaction.reactionId, createdReaction)
    )

  def deleteReaction(id: ReactionId): F[Either[ReactionError, Boolean]] =
    client
      .run(Request[F](Method.DELETE, baseUri / id.toString))
      .use { response =>
        response.status match {
          case Status.NoContent => cacheService.cleanExpiredEntries.as(Right(true))
          case status           => Left(DeletionError(s"HTTP error ${status.code}: ${status.reason}")).pure[F]
        }
      }
      .recoverWith { case error =>
        Left(NetworkError(s"Network error: ${error.getMessage}")).pure[F]
      }

  private def fetchReactionFromRemote(id: ReactionId): F[ReactionDetails] =
    makeRequest[ReactionDetails](
      Request[F](Method.GET, baseUri / id.toString),
      responseBody =>
        decode[ReactionDetails](responseBody).leftMap { error =>
          DecodingError(s"Failed to parse ReactionDetails: ${error.getMessage}")
        }
    ).flatTap(reaction => cacheService.putReactionDetails(id, reaction))

  private def makeRequest[A](
    request: Request[F],
    decodeFn: String => Either[ReactionError, A]
  ): F[A] =
    client
      .run(request)
      .use { response =>
        response.as[String].flatMap { responseBody =>
          response.status match {
            case status if status.isSuccess =>
              decodeFn(responseBody).fold(Concurrent[F].raiseError, Concurrent[F].pure)
            case Status.NotFound            =>
              Concurrent[F].raiseError(NotFoundError(s"Resource not found: ${request.uri}"))
            case status                     =>
              Concurrent[F].raiseError(HttpError(s"HTTP error ${status.code}: ${status.reason}"))
          }
        }
      }
      .recoverWith { case error =>
        Concurrent[F].raiseError(NetworkError(s"Network error: ${error.getMessage}"))
      }

}
