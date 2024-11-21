package app.units

import cats.effect.{IO, Resource}

import org.typelevel.log4cats.Logger
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

/**
 * Provides resources related to HTTP clients for the application.
 */
object ClientResources {

  /**
   * Creates a managed `Client` resource using the Ember HTTP client.
   *
   * @param logger
   *   A logger instance used for logging any events or errors.
   * @return
   *   A managed `Resource` that encapsulates an `Http4s` `Client[IO]` instance. The `Client` is properly cleaned up
   *   when the resource is released.
   */
  def clientResource(
    implicit logger: Logger[IO]
  ): Resource[IO, Client[IO]] =
    EmberClientBuilder.default[IO].build

}
