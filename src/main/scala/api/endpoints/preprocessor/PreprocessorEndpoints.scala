package api.endpoints.preprocessor

import cats.effect.IO
import cats.syntax.semigroupk.toSemigroupKOps

import io.circe.syntax.EncoderOps

import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder

import core.services.preprocessor.{MechanismService, ReactionService}
import core.domain.preprocessor.{MechanismDetails, Reaction, ReactionDetails}
import core.errors.http.preprocessor.ReactionError

/**
 * Defines the HTTP routes for handling reactions and mechanisms in the preprocessor.
 *
 * @param reactionService
 *   The service that handles reactions-related operations.
 * @param mechanismService
 *   The service that handles mechanisms-related operations.
 */
class PreprocessorEndpoints(
  reactionService:  ReactionService[IO],
  mechanismService: MechanismService[IO]
) {

  /**
   * HTTP GET route for fetching a reaction by its ID.
   *
   * @param id
   *   The ID of the reaction to fetch.
   * @return
   *   A `ReactionDetails` object in JSON format or an appropriate error response.
   */
  private val getReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "reaction" / id =>
      validateId(id) match {
        case Some(validId) =>
          reactionService.getReaction(validId).flatMap {
            (reactionDetails: ReactionDetails) => Ok(reactionDetails.asJson)
          }.handleErrorWith {
            case _: ReactionError.NotFoundError => NotFound(("NotFound", s"Reaction with ID $validId not found"))
            case ex                             => InternalServerError(("InternalError", ex.getMessage))
          }
        case None          => BadRequest(("BadRequest", "ID must be an integer"))
      }
  }

  /**
   * HTTP POST route for creating a new reaction.
   *
   * @param req
   *   The HTTP request containing the `Reaction` object in the body.
   * @return
   *   The created `Reaction` object in JSON format or an appropriate error response.
   */
  private val postReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "reaction" =>
      req.as[Reaction].flatMap { reaction =>
        reactionService.createReaction(reaction).flatMap {
          createdReaction => Created(createdReaction.asJson)
        }.handleErrorWith {
          case _: ReactionError.CreationError => BadRequest(("CreationError", "Failed to create reaction"))
          case ex                             => InternalServerError(("InternalError", ex.getMessage))
        }
      }
  }

  /**
   * HTTP DELETE route for deleting a reaction by its ID.
   *
   * @param id
   *   The ID of the reaction to delete.
   * @return
   *   HTTP 204 No Content on success, or an appropriate error response.
   */
  private val deleteReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case DELETE -> Root / "reaction" / id =>
      validateId(id) match {
        case Some(validId) =>
          reactionService.deleteReaction(validId).flatMap {
            case Right(_)    => NoContent()
            case Left(error) => BadRequest(("DeletionError", error.message))
          }
        case None          => BadRequest(("BadRequest", "ID must be an integer"))
      }
  }

  /**
   * HTTP GET route for fetching a mechanism by its ID.
   *
   * @param id
   *   The ID of the mechanism to fetch.
   * @return
   *   A `MechanismDetails` object in JSON format or an appropriate error response.
   */
  private val getMechanismRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "mechanism" / id =>
      validateId(id) match {
        case Some(validId) =>
          mechanismService.getMechanism(validId).flatMap {
            (mechanismDetails: MechanismDetails) => Ok(mechanismDetails.asJson)
          }.handleErrorWith {
            case _: ReactionError.NotFoundError => NotFound(("NotFound", s"Mechanism with ID $validId not found"))
            case ex                             => InternalServerError(("InternalError", ex.getMessage))
          }
        case None          => BadRequest(("BadRequest", "ID must be an integer"))
      }
  }

  private def validateId(id: String): Option[Int] = id.toIntOption

  val routes: HttpRoutes[IO] = Logger.httpRoutes(logHeaders = false, logBody = true)(
    Router(
      "/api" -> (getReactionRoute <+> postReactionRoute <+> deleteReactionRoute <+> getMechanismRoute)
    )
  )

}
