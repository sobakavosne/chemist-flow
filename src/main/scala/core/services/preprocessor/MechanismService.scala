package core.services.preprocessor

import cats.effect.Concurrent
import cats.implicits._

import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId}
import core.errors.http.preprocessor.MechanismError
import core.errors.http.preprocessor.MechanismError._
import core.services.cache.DistributedCacheService

import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}

import io.circe.syntax._
import io.circe.parser.decode

import org.http4s.circe._

/**
 * Service for managing mechanisms using both a distributed cache and remote HTTP service.
 *
 * This service provides methods to fetch, create, and delete mechanisms. It interacts with a distributed cache for
 * efficient data retrieval and synchronises with a remote service via HTTP for data persistence and updates.
 *
 * @param cacheService
 *   The distributed cache service used for storing and retrieving mechanisms.
 * @param client
 *   The HTTP client for making requests to the remote mechanism service.
 * @param baseUri
 *   The base URI of the remote mechanism service.
 * @tparam F
 *   The effect type (e.g., `IO`, `SyncIO`, etc.) that supports concurrency.
 */
class MechanismService[F[_]: Concurrent](
  cacheService: DistributedCacheService[F],
  client:       Client[F],
  baseUri:      Uri
) {

  /**
   * Fetches a mechanism by its ID.
   *
   * This method first checks the distributed cache for the requested mechanism. If the mechanism is not found in the
   * cache, it fetches the data from the remote mechanism service and updates the cache.
   *
   * @param id
   *   The unique identifier of the mechanism to fetch.
   * @return
   *   An effectful computation that yields the `MechanismDetails` for the given ID.
   */
  def getMechanism(id: MechanismId): F[MechanismDetails] =
    cacheService.getMechanismDetails(id).flatMap {
      case Some(cachedMechanism) => cachedMechanism.pure[F]
      case None                  => fetchMechanismFromRemote(id)
    }

  /**
   * Creates a new mechanism.
   *
   * This method sends a `POST` request to the remote mechanism service to create a new mechanism. The created mechanism
   * is then added to the distributed cache.
   *
   * @param mechanism
   *   The mechanism to create.
   * @return
   *   An effectful computation that yields the created `Mechanism` upon success.
   */
  def createMechanism(mechanism: Mechanism): F[Mechanism] =
    makeRequest[Mechanism](
      Request[F](Method.POST, baseUri).withEntity(mechanism.asJson),
      responseBody =>
        decode[Mechanism](responseBody).leftMap { error =>
          DecodingError(s"Failed to parse created Mechanism: ${error.getMessage}")
        }
    ).flatTap(createdMechanism =>
      cacheService.putMechanism(createdMechanism.mechanismId, createdMechanism)
    )

  /**
   * Deletes a mechanism by its ID.
   *
   * This method sends a `DELETE` request to the remote mechanism service. If the deletion is successful, the cache is
   * updated to remove any stale data.
   *
   * @param id
   *   The unique identifier of the mechanism to delete.
   * @return
   *   An effectful computation that yields:
   *   - `Right(true)` if the mechanism was successfully deleted.
   *   - `Left(MechanismError)` if an error occurred during deletion.
   */
  def deleteMechanism(id: MechanismId): F[Either[MechanismError, Boolean]] =
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

  private def fetchMechanismFromRemote(id: MechanismId): F[MechanismDetails] =
    makeRequest[MechanismDetails](
      Request[F](Method.GET, baseUri / id.toString),
      responseBody =>
        decode[MechanismDetails](responseBody).leftMap { error =>
          DecodingError(s"Failed to parse MechanismDetails: ${error.getMessage}")
        }
    ).flatTap(mechanism => cacheService.putMechanismDetails(id, mechanism))

  private def makeRequest[A](
    request: Request[F],
    decodeFn: String => Either[MechanismError, A]
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
