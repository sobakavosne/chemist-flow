package core.services

import cats.effect.Concurrent
import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxApplicativeId, catsSyntaxApplyOps, toFlatMapOps}
import core.domain.{Reaction, ReactionId}
import core.errors.http.ReactionError
import core.errors.http.ReactionError.{CreationError, DeletionError, NotFoundError}
import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}
import io.circe.syntax.EncoderOps
import org.http4s.circe.jsonEncoder
import org.http4s.circe.toMessageSyntax
import org.http4s.implicits.uri

class ReactionService[F[_]: Concurrent](
  client:       Client[F],
  cacheService: CacheService[F]
) {
  private val baseUri = uri"http://localhost:8080/reaction"

  def getReaction(id: ReactionId): F[Reaction] =
    cacheService
      .getReaction(id)
      .flatMap {
        case Some(cachedReaction) =>
          Concurrent[F].pure(cachedReaction)
        case None                 =>
          client.run(Request[F](Method.GET, baseUri / id.toString)).use { response =>
            response
              .decodeJson[Reaction]
              .attempt
              .flatMap {
                case Right(reaction) if response.status.isSuccess =>
                  cacheService.putReaction(id, reaction) *> Concurrent[F].pure(reaction)
                case _ if response.status == Status.NotFound      =>
                  Concurrent[F].raiseError(new NotFoundError(s"Reaction with ID $id not found"))
                case _                                            =>
                  Concurrent[F].raiseError(new RuntimeException(s"Failed to fetch Reaction with ID $id"))
              }
          }
      }

  def createReaction(Reaction: Reaction): F[Reaction] =
    client
      .run(Request[F](Method.POST, baseUri).withEntity(Reaction.asJson))
      .use { response =>
        response
          .decodeJson[Reaction]
          .attempt
          .flatMap {
            case Right(createdReaction) if response.status.isSuccess =>
              cacheService.putReaction(createdReaction.id, createdReaction)
              *> Concurrent[F].pure(createdReaction)
            case Right(_)    => Concurrent[F].raiseError(CreationError("Reaction could not be created"))
            case Left(error) => Concurrent[F].raiseError(
                CreationError(s"Failed to create Reaction: ${error.getMessage}")
              )
          }
      }

  def deleteReaction(id: ReactionId): F[Either[ReactionError, Boolean]] =
    client.run(Request[F](Method.DELETE, baseUri / id.toString)).use { response =>
      if (response.status == Status.NoContent) {
        cacheService.cleanExpiredEntries *> Right(true).pure[F]
      } else {
        Left(DeletionError(s"Failed to delete Reaction with ID: $id")).pure[F]
      }
    }

}
