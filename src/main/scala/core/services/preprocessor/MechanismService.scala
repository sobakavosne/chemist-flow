package core.services.preprocessor

import cats.effect.Concurrent
import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxApplicativeId, catsSyntaxApplyOps, toFlatMapOps}
import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId}
import core.errors.http.preprocessor.MechanismError
import core.errors.http.preprocessor.MechanismError.{CreationError, DeletionError, NotFoundError}
import core.services.cache.CacheService
import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}
import io.circe.syntax.EncoderOps
import org.http4s.circe.jsonEncoder
import org.http4s.circe.toMessageSyntax

class MechanismService[F[_]: Concurrent](
  client:       Client[F],
  cacheService: CacheService[F],
  baseUri:      Uri
)(
  // implicit logger:  org.typelevel.log4cats.Logger[cats.effect.IO]
) {

  def getMechanism(id: MechanismId): F[MechanismDetails] =
    cacheService
      .getMechanism(id)
      .flatMap {
        case Some(cachedMechanism) =>
          Concurrent[F].pure(cachedMechanism)
        case None                  =>
          client
            .run(Request[F](Method.GET, baseUri / id.toString))
            .use { response =>
              // logger.info(s"{response}")
              response
                .decodeJson[MechanismDetails]
                .attempt
                .flatMap {
                  case Right(mechanism) if response.status.isSuccess =>
                    cacheService.putMechanismDetails(id, mechanism) *> Concurrent[F].pure(mechanism)
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
              cacheService.putMechanism(createdMechanism.mechanismId, createdMechanism)
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
