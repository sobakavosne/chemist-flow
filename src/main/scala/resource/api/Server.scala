package resource.api

import cats.effect.{IO, Resource}
import com.comcast.ip4s.{Host, Port}
import cats.syntax.flatMap.toFlatMapOps
import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import org.slf4j.LoggerFactory

class ServerBuilder(
  implicit endpoints: Endpoints
) {
  private val logger = LoggerFactory.getLogger(getClass)

  def startServer(
    interface: Host,
    port: Port
  ): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(interface)
      .withPort(port)
      .withHttpApp(endpoints.routes.orNotFound)
      .build
      .flatTap { server =>
        Resource.eval(IO(logger.info(
          s"Server online at http://${server.address.getHostName}:${server.address.getPort}/" +
            s"\nPress ENTER to terminate..."
        )))
      }
  }
}
