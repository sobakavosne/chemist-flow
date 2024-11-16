package core.services.preprocessor

import cats.effect.Concurrent
import cats.implicits._
import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId}
import core.errors.http.preprocessor.MechanismError
import core.errors.http.preprocessor.MechanismError._
import core.services.cache.CacheService
import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}
import io.circe.syntax._
import io.circe.parser.decode
import org.http4s.circe._

class MechanismService[F[_]: Concurrent](
  client:       Client[F],
  cacheService: CacheService[F],
  baseUri:      Uri
) {

  def getMechanism(id: MechanismId): F[MechanismDetails] =
    cacheService.getMechanism(id).flatMap {
      case Some(cachedMechanism) => cachedMechanism.pure[F]
      case None                  => fetchMechanismFromRemote(id)
    }

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
