package resource.api

import cats.effect.IO
import cats.syntax.semigroupk.toSemigroupKOps
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.circe.CirceEntityEncoder._
import org.slf4j.LoggerFactory

class Endpoints {
  private val logger = LoggerFactory.getLogger(getClass)

  private val healthRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      logger.info("[Request]: get health check")
      Ok("Health check response")
  }

  private val getReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "reaction" / id =>
      logger.info(s"[Request]: get reaction details for ID: $id")
      Ok(s"Get reaction details for ID: $id")
  }

  private val postReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case POST -> Root / "reaction" =>
      logger.info("[Request]: create new reaction")
      Ok("Create new reaction")
  }

  private val deleteReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case DELETE -> Root / "reaction" / id =>
      logger.info(s"[Request]: delete reaction with ID: $id")
      Ok(s"Delete reaction with ID: $id")
  }

  val routes: HttpRoutes[IO] = Router(
    "/api" -> (healthRoute <+> getReactionRoute <+> postReactionRoute <+> deleteReactionRoute)
  )
}
