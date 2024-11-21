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
 * Defines the HTTP routes for handling reactions and mechanisms.
 *
 * This class provides endpoints for performing CRUD operations on reactions and retrieving mechanism details. Each
 * endpoint calls the appropriate service, and returns a response in JSON format.
 *
 * @param reactionService
 *   The service responsible for handling reaction-related operations, such as fetching, creating, and deleting
 *   reactions.
 * @param mechanismService
 *   The service responsible for handling mechanism-related operations, such as fetching mechanism details.
 */
class PreprocessorEndpoints(
  reactionService:  ReactionService[IO],
  mechanismService: MechanismService[IO]
) {

  /**
   * HTTP GET route for fetching a reaction by its ID.
   *
   * Endpoint: `/api/reaction/{id}`
   *
   * This route validates the provided ID, fetches the reaction details from the `ReactionService`, and returns the
   * details in JSON format. If the ID is invalid or the reaction is not found, it returns an appropriate error
   * response.
   *
   * @param id
   *   The string representation of the reaction ID to fetch.
   * @return
   *   - `200 OK` with the `ReactionDetails` object in JSON format if the reaction is found.
   *   - `400 Bad Request` if the ID is invalid.
   *   - `404 Not Found` if no reaction with the given ID exists.
   *   - `500 Internal Server Error` if an unexpected error occurs.
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
   * This route reads a `Reaction` object from the request body, passes it to the `ReactionService` to create a new
   * reaction, and returns the created reaction details in JSON format.
   *
   * @param req
   *   The HTTP request containing the `Reaction` object in the body.
   * @return
   *   - `201 Created` with the created `Reaction` object in JSON format on success.
   *   - `400 Bad Request` if the request body is invalid or creation fails.
   *   - `500 Internal Server Error` if an unexpected error occurs.
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
   * Endpoint: `/api/reaction/{id}`
   *
   * This route validates the provided ID, deletes the reaction using the `ReactionService`, and returns an appropriate
   * response. If the ID is invalid or the deletion fails, an error response is returned.
   *
   * @param id
   *   The string representation of the reaction ID to delete.
   * @return
   *   - `204 No Content` if the reaction is successfully deleted.
   *   - `400 Bad Request` if the ID is invalid or deletion fails.
   *   - `500 Internal Server Error` if an unexpected error occurs.
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
   * Endpoint: `/api/mechanism/{id}`
   *
   * This route validates the provided ID, fetches the mechanism details from the `MechanismService`, and returns the
   * details in JSON format. If the ID is invalid or the mechanism is not found, it returns an appropriate error
   * response.
   *
   * @param id
   *   The string representation of the mechanism ID to fetch.
   * @return
   *   - `200 OK` with the `MechanismDetails` object in JSON format if the mechanism is found.
   *   - `400 Bad Request` if the ID is invalid.
   *   - `404 Not Found` if no mechanism with the given ID exists.
   *   - `500 Internal Server Error` if an unexpected error occurs.
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

  /**
   * Validates the given string as an integer ID.
   *
   * @param id
   *   The string to validate.
   * @return
   *   An `Option[Int]` containing the valid integer ID, or `None` if the validation fails.
   */
  private def validateId(id: String): Option[Int] = id.toIntOption

  /**
   * Combines all defined HTTP routes and applies middleware for logging.
   *
   * @return
   *   A single `HttpRoutes[IO]` instance containing all endpoints.
   */
  val routes: HttpRoutes[IO] = Logger.httpRoutes(logHeaders = false, logBody = true)(
    Router(
      "/api" -> (getReactionRoute <+> postReactionRoute <+> deleteReactionRoute <+> getMechanismRoute)
    )
  )

}
