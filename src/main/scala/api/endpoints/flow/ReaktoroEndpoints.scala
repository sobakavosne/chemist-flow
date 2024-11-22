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

/**
 * Defines the HTTP routes for interacting with the ReaktoroService.
 *
 * This class sets up the routing for the API endpoints that allow clients to compute system properties for chemical
 * reactions. It integrates with the `ReaktoroService` to perform the computations and handle requests.
 *
 * @param reaktoroService
 *   The service handling system property computations. This should implement the core logic to process reaction
 *   properties using the provided inputs.
 */

class ReaktoroEndpoints(
  reaktoroService: ReaktoroService[IO]
) {

  /**
   * HTTP POST route for computing system properties for a chemical reaction.
   *
   * Endpoint: `/api/system/properties`
   *
   * This endpoint accepts a JSON payload containing a `ComputePropsRequest` object, which includes:
   *   - `reactionId`: The identifier of the reaction.
   *   - `database`: The database to use for the computation.
   *   - `amounts`: The list of molecule amounts relevant to the computation.
   *
   * The route invokes the `ReaktoroService` to compute the system properties for the given reaction and returns the
   * results as a JSON response. If an error occurs during processing, an appropriate error response is sent.
   *
   * @return
   *   An HTTP response:
   *   - `200 OK`: With a JSON body containing the computed system properties.
   *   - `500 Internal Server Error`: If the service fails to process the request.
   */

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
