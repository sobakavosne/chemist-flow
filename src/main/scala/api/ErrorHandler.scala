package api

import cats.effect.IO
import org.http4s._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityEncoder._

/**
 * Represents an error response returned by the server.
 *
 * @param error
 *   The type of error (e.g., "NotFound", "BadRequest").
 * @param message
 *   A descriptive message about the error.
 */
final case class ErrorResponse(error: String, message: String)

object ErrorHandler {

  /**
   * Wraps the provided HTTP routes with error handling logic.
   *
   * @param routes
   *   The `HttpRoutes[IO]` to be wrapped with error handling.
   * @return
   *   A new `HttpRoutes[IO]` that handles errors and returns appropriate HTTP responses with JSON error messages.
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
