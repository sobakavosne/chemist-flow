package api

import cats.effect.{IO, Resource}

import com.comcast.ip4s.{Host, Port}

import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes

class ServerBuilder(
  routes: HttpRoutes[IO]
) {

  def startServer(
    host: Host,
    port: Port
  ): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(host)
      .withPort(port)
      .withHttpApp(ErrorHandler(routes).orNotFound)
      .build
  }

}
