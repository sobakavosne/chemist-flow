package resource.api

import cats.effect.IO
import cats.syntax.semigroupk.toSemigroupKOps
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.http4s.circe.CirceEntityEncoder._

class Endpoints {
  private val healthRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      Ok("Health check response")
  }

  private val getReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "reaction" / id =>
      Ok(s"Get reaction details for ID: $id")
  }

  private val postReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case POST -> Root / "reaction" =>
      Ok("Create new reaction")
  }

  private val deleteReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case DELETE -> Root / "reaction" / id =>
      Ok(s"Delete reaction with ID: $id")
  }

  val routes: HttpRoutes[IO] = Logger.httpRoutes(logHeaders = false, logBody = true)(
    Router(
      "/api" -> (healthRoute <+> getReactionRoute <+> postReactionRoute <+> deleteReactionRoute)
    )
  )
}
