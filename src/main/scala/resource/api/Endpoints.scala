package api

import akka.actor.ActorSystem
import cats.effect.{IO, Resource}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class Endpoints {
  private def healthRoute: Route = path("health")(get(complete("Health check response")))

  private def getReactionRoute: Route =
    path("reaction" / Segment) { id =>
      get(complete(s"Get reaction details for ID: $id"))
    }

  private def postReactionRoute: Route = path("reaction")(post(complete("Create new reaction")))

  private def deleteReactionRoute: Route =
    path("reaction" / Segment) { id =>
      delete(complete(s"Delete reaction with ID: $id"))
    }

  val routes: Route =
    pathPrefix("api") {
      healthRoute ~ getReactionRoute ~ postReactionRoute ~ deleteReactionRoute
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
            println(
              s"Server online at http://${binding.localAddress.getHostName}:${binding.localAddress.getPort}/" +
                "\nPress ENTER to terminate..."
            )
          )
        }
    } { binding =>
      IO.fromFuture(IO(binding.unbind))
        .flatTap(_ => IO(println("Server unbound successfully")))
        .handleErrorWith(ex => IO(println(s"Failed to unbind server: ${ex.getMessage}")))
        .as(())
    }
  }
}
