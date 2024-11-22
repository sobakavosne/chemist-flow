package app.units

import akka.actor.ActorSystem
import akka.cluster.ddata.SelfUniqueAddress
import akka.util.Timeout

import cats.effect.{IO, Resource}

import core.services.cache.{DistributedCacheService, LocalCacheService}
import core.services.preprocessor.{MechanismService, ReactionService}
import core.services.flow.ReaktoroService

import org.http4s.client.Client
import org.http4s.Uri
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

/**
 * Provides managed resources for initialising and managing services in the application.
 *
 * This object encapsulates the lifecycle management of core services like `MechanismService`, `ReactionService`,
 * `ReaktoroService`, and various caching services. By using `Resource`, it ensures that resources are properly
 * initialised and cleaned up.
 */
object ServiceResources {

  /**
   * Creates a managed resource for the `MechanismService`.
   *
   * The `MechanismService` interacts with caching and HTTP APIs to manage mechanisms. This method ensures that the
   * service is initialised and cleaned up correctly, with logging for lifecycle events.
   *
   * Example usage:
   * {{{
   *   val mechanismResource = ServiceResources.mechanismServiceResource(cacheService, httpClient, baseUri)
   *   mechanismResource.use { mechanismService =>
   *     // Use the mechanismService
   *   }
   * }}}
   *
   * @param cacheService
   *   The distributed cache service used for storing and retrieving mechanisms.
   * @param client
   *   The HTTP client instance used for making API requests.
   * @param baseUri
   *   The base URI for the mechanism service's API endpoints.
   * @param logger
   *   An implicit logger instance for logging lifecycle events.
   * @return
   *   A `Resource[IO, MechanismService[IO]]` for the managed lifecycle of the `MechanismService`.
   */
  def mechanismServiceResource(
    cacheService: DistributedCacheService[IO],
    client: Client[IO],
    baseUri: Uri
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, MechanismService[IO]] =
    Resource.make(
      logger.info("Creating Mechanism Service") *>
        IO(new MechanismService[IO](cacheService, client, baseUri))
    )(_ =>
      logger.info("Shutting down Mechanism Service").handleErrorWith(_ => IO.unit)
    )

  /**
   * Creates a managed resource for the `ReactionService`.
   *
   * The `ReactionService` handles caching and API interactions for reactions. This method manages its lifecycle,
   * ensuring proper initialisation and cleanup with appropriate logging.
   *
   * @param cacheService
   *   The distributed cache service used for storing and retrieving reactions.
   * @param client
   *   The HTTP client instance used for making API requests.
   * @param baseUri
   *   The base URI for the reaction service's API endpoints.
   * @param logger
   *   An implicit logger instance for logging lifecycle events.
   * @return
   *   A `Resource[IO, ReactionService[IO]]` for the managed lifecycle of the `ReactionService`.
   */
  def reactionServiceResource(
    cacheService: DistributedCacheService[IO],
    client: Client[IO],
    baseUri: Uri
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, ReactionService[IO]] =
    Resource.make(
      logger.info("Creating Reaction Service") *>
        IO(new ReactionService[IO](cacheService, client, baseUri))
    )(_ =>
      logger.info("Shutting down Reaction Service").handleErrorWith(_ => IO.unit)
    )

  /**
   * Creates a managed resource for the `ReaktoroService`.
   *
   * The `ReaktoroService` builds on the `ReactionService` to provide extended functionality for managing reactions.
   * This method ensures its lifecycle is properly managed, with detailed logging for creation and shutdown.
   *
   * @param reactionService
   *   The `ReactionService` used for providing dependencies to the `ReaktoroService`.
   * @param client
   *   The HTTP client instance used for making API requests.
   * @param baseUri
   *   The base URI for the Reaktoro service's API endpoints.
   * @param logger
   *   An implicit logger instance for logging lifecycle events.
   * @return
   *   A `Resource[IO, ReaktoroService[IO]]` for the managed lifecycle of the `ReaktoroService`.
   */
  def reaktoroServiceResource(
    reactionService: ReactionService[IO],
    client: Client[IO],
    baseUri: Uri
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, ReaktoroService[IO]] =
    Resource.make(
      logger.info("Creating Reaktoro Service") *>
        IO(new ReaktoroService[IO](reactionService, client, baseUri))
    )(_ =>
      logger.info("Shutting down Reaktoro Service").handleErrorWith(_ => IO.unit)
    )

  /**
   * Creates a managed resource for the `LocalCacheService`.
   *
   * This method initialises a simple in-memory cache for local caching needs. The cache lifecycle is managed, and
   * events are logged during creation and release.
   *
   * @param ttl
   *   The time-to-live duration for cache entries.
   * @param logger
   *   An implicit logger instance for logging lifecycle events.
   * @return
   *   A `Resource[IO, LocalCacheService[IO]]` for the managed lifecycle of the `LocalCacheService`.
   */
  def localCacheServiceResource(
    implicit
    ttl: FiniteDuration,
    logger: Logger[IO]
  ): Resource[IO, LocalCacheService[IO]] =
    Resource.make(
      logger.info("Creating Local Cache Service") *>
        IO(new LocalCacheService[IO])
    )(_ =>
      logger.info("Shutting down Local Cache Service").handleErrorWith(_ => IO.unit)
    )

  /**
   * Creates a managed resource for the `DistributedCacheService`.
   *
   * This method initialises a distributed cache backed by Akka Cluster. It ensures proper integration with the actor
   * system and cluster configuration while managing the lifecycle and logging events.
   *
   * @param system
   *   The `ActorSystem` used for Akka-based concurrency and distributed data.
   * @param selfUniqueAddress
   *   The unique address of the current actor system instance, used for cluster data.
   * @param ex
   *   An implicit `ExecutionContext` for asynchronous operations.
   * @param ttl
   *   The time-to-live duration for cache entries.
   * @param logger
   *   An implicit logger instance for logging lifecycle events.
   * @return
   *   A `Resource[IO, DistributedCacheService[IO]]` for the managed lifecycle of the `DistributedCacheService`.
   */
  def distributedCacheServiceResource(
    system: ActorSystem,
    selfUniqueAddress: SelfUniqueAddress
  )(
    implicit
    ex: ExecutionContext,
    ttl: Timeout,
    logger: Logger[IO]
  ): Resource[IO, DistributedCacheService[IO]] =
    Resource.make(
      logger.info("Creating Distributed Cache Service") *>
        IO(new DistributedCacheService[IO](system, selfUniqueAddress))
    )(_ =>
      logger.info("Shutting down Distributed Cache Service").handleErrorWith(_ => IO.unit)
    )

}
