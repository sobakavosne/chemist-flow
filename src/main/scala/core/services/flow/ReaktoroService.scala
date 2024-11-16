package core.services.flow

import cats.effect.IO

import io.circe.generic.auto.deriveDecoder

import core.services.preprocessor.ReactionService
import core.domain.preprocessor.{Molecule, ReactionDetails, ReactionId}
import core.domain.flow.{ChemicalProps, SystemState}
import core.errors.http.flow.ChemicalPropsError
import core.errors.http.flow.ChemicalPropsError.ChemistEngineError

import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.circe.toMessageSyntax
import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}
import core.domain.flow.DataBase

class ReaktoroService(
  reactionService:     ReactionService[IO],
  chemistEngineClient: Client[IO],
  chemistEngineUri:    Uri
) {

  def computeChemicalProps(
    reactionId: ReactionId,
    temperature: Double,
    pressure: Double,
    database: DataBase
  ): IO[Either[ChemicalPropsError, ChemicalProps]] = {

    reactionService.getReaction(reactionId).attempt.flatMap {
      case Right(reactionDetails)          =>
        // val accelerate: List[ACCELERATE] = reactionDetails.conditions.map(_._1)

        val systemState = new SystemState(
          temperature,
          pressure,
          database,
          moleculeAmounts = Map()
        )

        sendToChemistEngine(systemState)
      case Left(error: ChemistEngineError) => IO.pure(Left(error))
      case Left(error: Throwable)          => IO.raiseError(error)
    }
  }

  private def sendToChemistEngine(
    systemState: SystemState
  ): IO[Either[ChemicalPropsError, ChemicalProps]] = {

    val request = Request[IO](Method.POST, chemistEngineUri).withEntity(systemState)

    chemistEngineClient.run(request).use { response =>
      if (response.status.isSuccess)                   {
        response
          .decodeJson[ChemicalProps]
          .map(Right(_))
      } else if (response.status == Status.BadRequest) {
        response.as[String].map { errorMessage =>
          Left(new ChemicalPropsError.BadRequestError(errorMessage))
        }
      } else {
        IO.pure(Left(new ChemistEngineError("Failed to compute ChemicalProps")))
      }
    }
  }

}
