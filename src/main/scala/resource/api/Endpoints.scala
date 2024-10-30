package api

import akka.actor.ActorSystem
import cats.effect.{IO, Resource}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory

class Endpoints {
  private val logger = LoggerFactory.getLogger(getClass)

  private def healthRoute: Route =
    path("health")(get {
      logger.info("[Request]: get health check")
      complete("Health check response")
    })

  private def getReactionRoute: Route =
    path("reaction" / Segment) { id =>
      get {
        logger.info(s"[Request]: get reaction details for ID: $id")
        complete(s"Get reaction details for ID: $id")
      }
    }

  private def postReactionRoute: Route =
    path("reaction")(post {
      logger.info("[Request]: create new reaction")
      complete("Create new reaction")
    })

  private def deleteReactionRoute: Route =
    path("reaction" / Segment) { id =>
      delete {
        logger.info(s"[Request]: delete reaction with ID: $id")
        complete(s"Delete reaction with ID: $id")
      }
    }

  private val routes: Route =
    pathPrefix("api") {
      healthRoute ~
      getReactionRoute ~
      postReactionRoute ~
      deleteReactionRoute
    }

  def startServer(
    interface: String,
    port: Int
  )(
    implicit system: ActorSystem
  ): Resource[IO, Http.ServerBinding] = {
    Resource.make {
      IO.fromFuture(IO(Http().newServerAt(interface, port).bind(routes)))
        .flatTap { binding =>
          IO(
            logger.info(
              s"Server online at http://${binding.localAddress.getHostName}:${binding.localAddress.getPort}/" +
                "\nPress ENTER to terminate..."
            )
          )
        }
    } { binding =>
      IO.fromFuture(IO(binding.unbind))
        .flatTap(_ => IO(logger.info("Server unbound successfully")))
        .handleErrorWith(ex => IO(logger.error(s"Failed to unbind server: ${ex.getMessage}")))
        .as(())
    }
  }
}
