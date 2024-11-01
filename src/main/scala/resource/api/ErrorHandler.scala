package resource.api

import cats.effect.IO
import org.http4s._
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityEncoder._

final case class ErrorResponse(error: String, message: String)

object ErrorHandler {
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
