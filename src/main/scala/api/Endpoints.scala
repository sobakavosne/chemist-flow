package api

import cats.effect.IO
import cats.syntax.semigroupk.toSemigroupKOps
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import io.circe.syntax.EncoderOps
import io.circe.generic.auto._

class Endpoints {

  private val healthRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      Ok("Health check response")
  }

  private val getReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "reaction" / id =>
      validateId(id) match {
        case Some(validId) => Ok(s"Get reaction details for ID: $validId")
        case None          => BadRequest(ErrorResponse("BadRequest", "ID must be an integer").asJson)
      }
  }

  private val postReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case POST -> Root / "reaction" =>
      Ok("Create new reaction")
  }

  private val deleteReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case DELETE -> Root / "reaction" / id =>
      validateId(id) match {
        case Some(validId) => Ok(s"Delete reaction with ID: $validId")
        case None          => BadRequest(ErrorResponse("BadRequest", "ID must be an integer").asJson)
      }
  }

  private def validateId(id: String): Option[Int] = id.toIntOption

  val routes: HttpRoutes[IO] = Logger.httpRoutes(logHeaders = false, logBody = true)(
    Router(
      "/api" -> (healthRoute <+> getReactionRoute <+> postReactionRoute <+> deleteReactionRoute)
    )
  )

}
