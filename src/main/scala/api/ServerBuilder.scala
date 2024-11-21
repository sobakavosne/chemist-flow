package api

import cats.effect.{IO, Resource}

import com.comcast.ip4s.{Host, Port}

import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes

/**
 * A class responsible for building and starting an HTTP server.
 *
 * @param routes
 *   The HTTP routes to be served by the server.
 */
class ServerBuilder(
  routes: HttpRoutes[IO]
) {

  /**
   * Starts the HTTP server with the specified host and port configuration.
   *
   * @param host
   *   The host address on which the server will listen.
   * @param port
   *   The port number on which the server will listen.
   * @return
   *   A `Resource[IO, Server]` that represents the running HTTP server. The server will be properly managed and
   *   terminated when the `Resource` is released.
   */
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
