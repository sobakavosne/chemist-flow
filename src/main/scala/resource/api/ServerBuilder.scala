package resource.api

import cats.effect.{IO, Resource}
import com.comcast.ip4s.{Host, Port}
import cats.syntax.flatMap.toFlatMapOps
import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger

class ServerBuilder(
  implicit
  endpoints: Endpoints,
  logger: Logger[IO]
) {
  def startServer(
    host: Host,
    port: Port
  ): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(host)
      .withPort(port)
      .withHttpApp(endpoints.routes.orNotFound)
      .build
      .flatTap { server => Resource.eval(logger.info("Press ENTER to terminate...")) }
  }
}
