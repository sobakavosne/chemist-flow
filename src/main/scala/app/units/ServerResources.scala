package app.units

import api.ServerBuilder

import cats.effect.{IO, Resource}

import org.typelevel.log4cats.Logger
import org.http4s.HttpRoutes

/**
 * Provides resources related to server management for the application.
 */
object ServerResources {

  /**
   * Creates a managed resource for the `ServerBuilder`.
   *
   * @param routes
   *   The `HttpRoutes[IO]` containing the API routes to be served by the server.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
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
      logger.info("Shutting down ServerBuilder").handleErrorWith(_ => IO.unit)
    )

}
