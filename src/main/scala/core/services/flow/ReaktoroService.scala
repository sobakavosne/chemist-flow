package core.services.flow

import cats.effect.Concurrent
import cats.implicits._
import cats.effect.kernel.implicits.parallelForGenSpawn

import core.services.preprocessor.ReactionService
import core.domain.preprocessor.{ACCELERATE, Molecule, ReactionDetails, ReactionId}
import core.domain.flow.{DataBase, MoleculeAmountList, SystemProps, SystemState}
import core.errors.http.flow.SystemPropsError
import core.errors.http.flow.SystemPropsError.{BadRequestError, ChemistEngineError}

import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.circe.toMessageSyntax
import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}

class ReaktoroService[F[_]: Concurrent](
  reactionService:     ReactionService[F],
  chemistEngineClient: Client[F],
  chemistEngineUri:    Uri
) {

  def computeSystemPropsForReaction(
    reactionId: ReactionId,
    database: DataBase,
    amounts: MoleculeAmountList
  ): F[List[Either[SystemPropsError, SystemProps]]] =
    reactionService
      .getReaction(reactionId)
      .map(reactionDetails => createSystemStateList(reactionDetails, database, amounts))
      .attempt
      .flatMap {
        case Right(systemStates)             => sendBatchToChemistEngine(systemStates)
        case Left(error: ChemistEngineError) => List(Left(error)).pure[F]
        case Left(error)                     => Concurrent[F].raiseError(error)
      }

  private def createSystemStateList(
    reactionDetails: ReactionDetails,
    database: DataBase,
    amounts: MoleculeAmountList
  ): List[SystemState] = {
    val accelerates: List[ACCELERATE] = reactionDetails.conditions.map(_._1)

    val moleculeAmounts: Map[Molecule, Double] = computeMoleculeAmounts(reactionDetails, amounts)

    accelerates.flatMap { accelerate =>
      accelerate
        .temperature
        .zip(accelerate.pressure)
        .map {
          case (temp, pres) =>
            SystemState(
              temperature = temp.toDouble,
              pressure    = pres.toDouble,
              database,
              moleculeAmounts
            )
        }
    }
  }

  private def computeMoleculeAmounts(
    reactionDetails: ReactionDetails,
    amounts: MoleculeAmountList
  ): Map[Molecule, Double] = {
    val inboundAmounts = reactionDetails.inboundReagents.zip(amounts.inboundReagentAmounts).map {
      case ((_, molecule), amount) => molecule -> amount
    }

    val outboundAmounts = reactionDetails.outboundProducts.zip(amounts.outboundProductAmounts).map {
      case ((_, molecule), amount) => molecule -> amount
    }

    (inboundAmounts ++ outboundAmounts).toMap
  }

  private def sendBatchToChemistEngine(
    systemStates: List[SystemState]
  ): F[List[Either[SystemPropsError, SystemProps]]] =
    systemStates.parTraverse(sendToChemistEngine)

  private def sendToChemistEngine(
    systemState: SystemState
  ): F[Either[SystemPropsError, SystemProps]] =
    chemistEngineClient
      .run(Request[F](Method.POST, chemistEngineUri).withEntity(systemState))
      .use { response =>
        response.status match {
          case status if status.isSuccess =>
            response.decodeJson[SystemProps].map(Right(_))
          case Status.BadRequest          =>
            response.as[String].map(msg => Left(BadRequestError(msg)))
          case _                          =>
            Concurrent[F].pure(Left(ChemistEngineError("Failed to compute SystemProps")))
        }
      }

}
