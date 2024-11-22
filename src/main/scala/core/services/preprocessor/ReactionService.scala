package core.services.preprocessor

import cats.effect.Concurrent
import cats.implicits._
import core.domain.preprocessor.{Reaction, ReactionDetails, ReactionId}
import core.errors.http.preprocessor.ReactionError
import core.errors.http.preprocessor.ReactionError._
import core.services.cache.DistributedCacheService
import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}
import io.circe.syntax._
import io.circe.parser.decode
import org.http4s.circe._

/**
 * Service for managing reactions using both a distributed cache and remote HTTP service.
 *
 * This service provides methods to fetch, create, and delete reactions. It integrates with a distributed cache for
 * efficient data retrieval and interacts with a remote service via HTTP for data persistence and updates.
 *
 * @param distributedCache
 *   The distributed cache service used for storing and retrieving reactions.
 * @param client
 *   The HTTP client for making requests to the remote reaction service.
 * @param baseUri
 *   The base URI of the remote reaction service.
 * @tparam F
 *   The effect type (e.g., `IO`, `SyncIO`, etc.) that supports concurrency.
 */
class ReactionService[F[_]: Concurrent](
  distributedCache: DistributedCacheService[F],
  client:           Client[F],
  baseUri:          Uri
) {

  /**
   * Fetches a reaction by its ID.
   *
   * This method first checks the distributed cache for the requested reaction. If the reaction is not found in the
   * cache, it fetches the data from the remote reaction service and updates the cache.
   *
   * @param id
   *   The unique identifier of the reaction to fetch.
   * @return
   *   An effectful computation that yields the `ReactionDetails` for the given ID.
   */
  def getReaction(id: ReactionId): F[ReactionDetails] =
    distributedCache.getReactionDetails(id).flatMap {
      case Some(cachedReaction) => cachedReaction.pure[F]
      case None                 => fetchReactionFromRemote(id)
    }

  /**
   * Creates a new reaction.
   *
   * This method sends a `POST` request to the remote reaction service to create a new reaction. The created reaction is
   * then added to the distributed cache.
   *
   * @param reaction
   *   The reaction to create.
   * @return
   *   An effectful computation that yields the created `Reaction` upon success.
   */
  def createReaction(reaction: Reaction): F[Reaction] =
    makeRequest[Reaction](
      Request[F](Method.POST, baseUri).withEntity(reaction.asJson),
      responseBody =>
        decode[Reaction](responseBody).leftMap { error =>
          CreationError(s"Failed to create Reaction: ${error.getMessage}")
        }
    ).flatTap(createdReaction =>
      distributedCache.putReaction(createdReaction.reactionId, createdReaction)
    )

  /**
   * Deletes a reaction by its ID.
   *
   * This method sends a `DELETE` request to the remote reaction service. If the deletion is successful, the cache is
   * updated to remove any stale data.
   *
   * @param id
   *   The unique identifier of the reaction to delete.
   * @return
   *   An effectful computation that yields:
   *   - `Right(true)` if the reaction was successfully deleted.
   *   - `Left(ReactionError)` if an error occurred during deletion.
   */
  def deleteReaction(id: ReactionId): F[Either[ReactionError, Boolean]] =
    client
      .run(Request[F](Method.DELETE, baseUri / id.toString))
      .use { response =>
        response.status match {
          case Status.NoContent => distributedCache.cleanExpiredEntries.as(Right(true))
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
    ).flatTap(reaction => distributedCache.putReactionDetails(id, reaction))

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
