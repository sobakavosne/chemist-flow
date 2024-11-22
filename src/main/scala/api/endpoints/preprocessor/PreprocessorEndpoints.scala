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
 * Provides HTTP endpoints for managing reactions and mechanisms in the preprocessor module.
 *
 * This class defines routes for:
 *   - Fetching reaction and mechanism details by ID.
 *   - Creating new reactions.
 *   - Deleting existing reactions.
 *
 * All endpoints interact with the `ReactionService` and `MechanismService` for business logic and return appropriate
 * JSON responses or error messages.
 *
 * @param reactionService
 *   Handles CRUD operations related to reactions.
 * @param mechanismService
 *   Handles retrieval of mechanism details.
 */
class PreprocessorEndpoints(
  reactionService:  ReactionService[IO],
  mechanismService: MechanismService[IO]
) {

  /**
   * HTTP GET route for fetching reaction details by ID.
   *
   * Endpoint: `/api/reaction/{id}`
   *
   * Validates the provided reaction ID, fetches the corresponding details using the `ReactionService`, and returns them
   * in JSON format. Returns an appropriate error response if:
   *   - The ID is invalid.
   *   - The reaction is not found.
   *   - An unexpected error occurs during processing.
   *
   * @param id
   *   The string representation of the reaction ID.
   * @return
   *   - `200 OK`: Contains the `ReactionDetails` in JSON format.
   *   - `400 Bad Request`: If the ID is invalid.
   *   - `404 Not Found`: If no reaction exists with the given ID.
   *   - `500 Internal Server Error`: For unexpected errors.
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
   * Endpoint: `/api/reaction`
   *
   * Accepts a `Reaction` object in the request body and uses the `ReactionService` to create a new reaction. Returns
   * the created reaction details in JSON format or an error response in case of failure.
   *
   * @param req
   *   The HTTP request containing the `Reaction` object.
   * @return
   *   - `201 Created`: Contains the created `Reaction` in JSON format.
   *   - `400 Bad Request`: If the request body is invalid or creation fails.
   *   - `500 Internal Server Error`: For unexpected errors.
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
   * HTTP DELETE route for deleting a reaction by ID.
   *
   * Endpoint: `/api/reaction/{id}`
   *
   * Validates the provided reaction ID and attempts to delete the reaction using the `ReactionService`. Returns an
   * appropriate response based on the success or failure of the operation.
   *
   * @param id
   *   The string representation of the reaction ID.
   * @return
   *   - `204 No Content`: If the reaction is successfully deleted.
   *   - `400 Bad Request`: If the ID is invalid or deletion fails.
   *   - `500 Internal Server Error`: For unexpected errors.
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
   * HTTP GET route for fetching mechanism details by ID.
   *
   * Endpoint: `/api/mechanism/{id}`
   *
   * Validates the provided mechanism ID, fetches the corresponding details using the `MechanismService`, and returns
   * them in JSON format. Returns an appropriate error response if:
   *   - The ID is invalid.
   *   - The mechanism is not found.
   *   - An unexpected error occurs during processing.
   *
   * @param id
   *   The string representation of the mechanism ID.
   * @return
   *   - `200 OK`: Contains the `MechanismDetails` in JSON format.
   *   - `400 Bad Request`: If the ID is invalid.
   *   - `404 Not Found`: If no mechanism exists with the given ID.
   *   - `500 Internal Server Error`: For unexpected errors.
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
