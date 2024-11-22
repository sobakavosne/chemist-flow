package api

import cats.effect.IO
import org.http4s._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityEncoder._

/**
 * Represents a standard error response returned by the server in JSON format.
 *
 * This case class is used to encapsulate error details, making it easy to serialise into a consistent JSON structure
 * for HTTP error responses.
 *
 * @param error
 *   A string identifying the type of error (e.g., "NotFound", "BadRequest").
 * @param message
 *   A descriptive message providing additional details about the error.
 */
final case class ErrorResponse(error: String, message: String)

/**
 * Provides error handling for HTTP routes in a standardised way.
 *
 * The `ErrorHandler` wraps existing HTTP routes and ensures that:
 *   - `NotFound` errors return a `404` status with a JSON-encoded `ErrorResponse`.
 *   - Validation errors (e.g., `IllegalArgumentException`) return a `400` status.
 *   - Unexpected errors return a `500` status with a generic message.
 *
 * This utility promotes consistent error handling across the application.
 */
object ErrorHandler {

  /**
   * Wraps the provided HTTP routes with error handling logic.
   *
   * This function ensures that errors raised during route processing are captured and translated into appropriate HTTP
   * responses with JSON-encoded error messages.
   *
   * Error Handling:
   *   - `NoSuchElementException`: Translates to a `404 Not Found` response.
   *   - `IllegalArgumentException`: Translates to a `400 Bad Request` response.
   *   - Any other exception: Translates to a `500 Internal Server Error` response.
   *
   * @param routes
   *   The `HttpRoutes[IO]` to be wrapped with error handling.
   * @return
   *   A new `HttpRoutes[IO]` that handles errors consistently and returns meaningful responses.
   */
  def apply(routes: HttpRoutes[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] { request =>
    routes(request).getOrElseF(
      IO(Response(Status.NotFound).withEntity(ErrorResponse(
        "NotFound",
        "Resource not found"
      ).asJson))
    ).handleErrorWith {
      case _: NoSuchElementException =>
        IO.pure(Response(Status.NotFound).withEntity(ErrorResponse(
          "NotFound",
          "Resource not found"
        ).asJson))

      case ex: IllegalArgumentException =>
        IO.pure(Response(Status.BadRequest).withEntity(ErrorResponse(
          "BadRequest",
          ex.getMessage
        ).asJson))

      case ex: Exception =>
        IO.pure(Response(Status.InternalServerError).withEntity(ErrorResponse(
          "InternalServerError",
          "An unexpected error occurred."
        ).asJson))
    }
  }

}
