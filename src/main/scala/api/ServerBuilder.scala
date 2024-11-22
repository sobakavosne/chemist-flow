package api

import cats.effect.{IO, Resource}

import com.comcast.ip4s.{Host, Port}

import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes

/**
 * A utility class for constructing and starting an HTTP server.
 *
 * This class uses the `EmberServerBuilder` to set up a server with the provided HTTP routes. It integrates error
 * handling for routes and ensures the server is properly managed as a `Resource`, allowing safe startup and shutdown.
 *
 * @param routes
 *   The HTTP routes to be served by the server.
 */
class ServerBuilder(
  routes: HttpRoutes[IO]
) {

  /**
   * Configures and starts an HTTP server with the specified host and port.
   *
   * This method uses the `EmberServerBuilder` to create and manage the server. It wraps the provided routes with the
   * `ErrorHandler` to ensure consistent error handling, and builds an HTTP application that listens on the specified
   * host and port.
   *
   * @param host
   *   The host address on which the server will listen (e.g., `Host.fromString("127.0.0.1")`).
   * @param port
   *   The port number on which the server will listen (e.g., `Port.fromInt(8080)`).
   * @return
   *   A `Resource[IO, Server]` that represents the running HTTP server. The server is automatically cleaned up when the
   *   `Resource` is released.
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
