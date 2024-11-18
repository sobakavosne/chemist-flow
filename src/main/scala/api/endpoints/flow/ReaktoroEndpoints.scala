package api.endpoints.flow

import cats.effect.IO

import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder

import io.circe.syntax._
import io.circe.generic.auto.deriveEncoder

import core.services.flow.ReaktoroService
import core.domain.preprocessor.ReactionId
import core.domain.flow.{DataBase, MoleculeAmountList}

case class ComputePropsRequest(
  reactionId: ReactionId,
  database:   DataBase,
  amounts:    MoleculeAmountList
)

object ComputePropsRequest {
  import io.circe.{Decoder, Encoder}
  import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

  implicit val decoder: Decoder[ComputePropsRequest] = deriveDecoder
  implicit val encoder: Encoder[ComputePropsRequest] = deriveEncoder
}

class ReaktoroEndpoints(
  reaktoroService: ReaktoroService[IO]
) {

  private val computeSystemPropsForReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "system" / "properties" =>
      req.as[ComputePropsRequest].flatMap {
        case ComputePropsRequest(reactionId, database, amounts) =>
          reaktoroService
            .computeSystemPropsForReaction(reactionId, database, amounts)
            .flatMap(result => Ok(result.asJson))
            .handleErrorWith(ex => InternalServerError(("InternalError", ex.getMessage).asJson))
      }
  }

  val routes: HttpRoutes[IO] = Logger.httpRoutes(logHeaders = true, logBody = true)(
    Router(
      "/api" -> computeSystemPropsForReactionRoute
    )
  )

}
