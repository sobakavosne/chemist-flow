package app.units

import cats.effect.{IO, Resource}

import org.typelevel.log4cats.Logger
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

/**
 * Provides resources for creating and managing HTTP clients in the application.
 *
 * This object encapsulates logic for building `Http4s` HTTP clients as managed resources, ensuring proper lifecycle
 * management (e.g., cleanup after use). It integrates seamlessly with `Cats Effect` to provide a safe and composable
 * way to work with clients.
 */
object ClientResources {

  /**
   * Creates a managed HTTP client resource using the Ember HTTP client.
   *
   * The `EmberClientBuilder` is used to construct a default `Http4s` client. The client is wrapped in a `Resource` to
   * ensure proper lifecycle management, including cleanup when the resource is released. The method also supports
   * logging via the provided `Logger` instance.
   *
   * Example usage:
   * {{{
   *   import org.typelevel.log4cats.slf4j.Slf4jLogger
   *   import app.units.ClientResources
   *
   *   implicit val logger = Slf4jLogger.getLogger[IO]
   *
   *   val clientResource = ClientResources.clientResource
   *   clientResource.use { client =>
   *     // Use the client to make HTTP requests
   *   }
   * }}}
   *
   * @param logger
   *   A logger instance for logging events or errors.
   * @return
   *   A managed `Resource` that encapsulates an `Http4s` `Client[IO]` instance.
   */
  def clientResource(
    implicit logger: Logger[IO]
  ): Resource[IO, Client[IO]] =
    EmberClientBuilder.default[IO].build

}
