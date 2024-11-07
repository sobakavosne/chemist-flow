package core.services

import cats.effect.Concurrent
import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxApplicativeId, catsSyntaxApplyOps, toFlatMapOps}
import core.domain.{Mechanism, MechanismId}
import core.errors.http.MechanismError
import core.errors.http.MechanismError.{CreationError, DeletionError, NotFoundError}
import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}
import io.circe.syntax.EncoderOps
import org.http4s.circe.jsonEncoder
import org.http4s.circe.toMessageSyntax
import org.http4s.implicits.uri

class MechanismService[F[_]: Concurrent](
  client:       Client[F],
  cacheService: CacheService[F]
) {
  private val baseUri = uri"http://localhost:8080/mechanism"

  def getMechanism(id: MechanismId): F[Mechanism] =
    cacheService
      .getMechanism(id)
      .flatMap {
        case Some(cachedMechanism) =>
          Concurrent[F].pure(cachedMechanism)
        case None                  =>
          client
            .run(Request[F](Method.GET, baseUri / id.toString))
            .use { response =>
              response
                .decodeJson[Mechanism]
                .attempt
                .flatMap {
                  case Right(mechanism) if response.status.isSuccess =>
                    cacheService.putMechanism(id, mechanism) *> Concurrent[F].pure(mechanism)
                  case _ if response.status == Status.NotFound       =>
                    Concurrent[F].raiseError(new NotFoundError(s"Mechanism with ID $id not found"))
                  case _                                             =>
                    Concurrent[F].raiseError(new RuntimeException(s"Failed to fetch Mechanism with ID $id"))
                }
            }
      }

  def createMechanism(mechanism: Mechanism): F[Mechanism] =
    client
      .run(Request[F](Method.POST, baseUri).withEntity(mechanism.asJson))
      .use { response =>
        response
          .decodeJson[Mechanism]
          .attempt
          .flatMap {
            case Right(createdMechanism) if response.status.isSuccess =>
              cacheService.putMechanism(createdMechanism.id, createdMechanism)
              *> Concurrent[F].pure(createdMechanism)
            case Right(_)    => Concurrent[F].raiseError(CreationError("Mechanism could not be created"))
            case Left(error) => Concurrent[F].raiseError(
                CreationError(s"Failed to create Mechanism: ${error.getMessage}")
              )
          }
      }

  def deleteMechanism(id: MechanismId): F[Either[MechanismError, Boolean]] =
    client.run(Request[F](Method.DELETE, baseUri / id.toString)).use { response =>
      if (response.status == Status.NoContent) {
        cacheService.cleanExpiredEntries *> Right(true).pure[F]
      } else {
        Left(DeletionError(s"Failed to delete Mechanism with ID: $id")).pure[F]
      }
    }

}
