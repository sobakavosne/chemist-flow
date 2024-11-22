package app.units

import api.ServerBuilder

import cats.effect.{IO, Resource}

import org.typelevel.log4cats.Logger
import org.http4s.HttpRoutes

/**
 * Provides managed resources for server management in the application.
 *
 * This object encapsulates the creation and lifecycle management of the `ServerBuilder`, ensuring that the server is
 * properly initialised and cleaned up as a managed resource.
 */
object ServerResources {

  /**
   * Creates a managed resource for the `ServerBuilder`.
   *
   * This method initialises and manages the lifecycle of a `ServerBuilder` instance, which serves the provided API
   * routes. It logs lifecycle events during resource creation and cleanup for better observability.
   *
   * Example usage:
   * {{{
   *   import org.typelevel.log4cats.slf4j.Slf4jLogger
   *   import app.units.ServerResources
   *
   *   implicit val logger = Slf4jLogger.getLogger[IO]
   *
   *   val serverResource = ServerResources.serverBuilderResource(myRoutes)
   *   serverResource.use { serverBuilder =>
   *     val server = serverBuilder.startServer(Host.fromString("127.0.0.1").get, Port.fromInt(8080).get)
   *     server.useForever
   *   }
   * }}}
   *
   * @param routes
   *   The `HttpRoutes[IO]` containing the API routes to be served by the server.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors.
   * @return
   *   A `Resource[IO, ServerBuilder]` that manages the lifecycle of the `ServerBuilder` instance.
   */
  def serverBuilderResource(
    routes: HttpRoutes[IO]
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, ServerBuilder] =
    Resource.make(
      logger.info("Creating Server Builder") *>
        IO(new ServerBuilder(routes))
    )(endpoints =>
      logger
        .info("Shutting down ServerBuilder")
        .handleErrorWith(_ => IO.unit)
    )

}
