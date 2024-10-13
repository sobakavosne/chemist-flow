package api

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future

class Endpoints(implicit system: ActorSystem) {
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private def healthRoute =
    path("health")(get(complete("Health check response")))

  private def getReactionRoute = path("reaction" / Segment) { id =>
    get {
      complete(s"Get reaction details for ID: $id")
    }
  }

  private def postReactionRoute =
    path("reaction")(post(complete("Create new reaction")))

  private def deleteReactionRoute = path("reaction" / Segment) { id =>
    delete {
      complete(s"Delete reaction with ID: $id")
    }
  }

  val routes = pathPrefix("api") {
    healthRoute ~
    getReactionRoute ~
    postReactionRoute ~
    deleteReactionRoute
  }

  def startServer(interface: String, port: Int): Future[Http.ServerBinding] = {
    val bindingFuture = Http().newServerAt(interface, port).bind(routes)

    bindingFuture.foreach { binding =>
      println(
        s"Server online at http://${binding.localAddress.getHostName}:${binding.localAddress.getPort}/"
      )
    }

    bindingFuture
  }
}
