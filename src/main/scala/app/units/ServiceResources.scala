package app.units

import akka.actor.ActorSystem
import akka.cluster.ddata.SelfUniqueAddress

import cats.effect.{IO, Resource}

import core.services.cache.CacheService
import core.services.cache.DistributedCacheService
import core.services.preprocessor.{MechanismService, ReactionService}
import core.services.flow.ReaktoroService

import org.http4s.client.Client
import org.http4s.Uri
import org.typelevel.log4cats.Logger

/**
 * Provides resources for service initialisation and lifecycle management.
 *
 * This object contains methods to create managed resources for various application services, ensuring proper
 * initialisation and cleanup using the `Resource` abstraction.
 */
object ServiceResources {

  /**
   * Creates a managed resource for the `MechanismService`.
   *
   * @param cacheService
   *   The distributed cache service used for storing and retrieving mechanisms.
   * @param client
   *   The HTTP client instance used to make requests to the mechanism service endpoints.
   * @param baseUri
   *   The base URI for the mechanism service's API endpoints.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, MechanismService[IO]]` that ensures the lifecycle of the `MechanismService` is managed correctly.
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
   * @param cacheService
   *   The distributed cache service used for storing and retrieving reactions.
   * @param client
   *   The HTTP client instance used to make requests to the reaction service endpoints.
   * @param baseUri
   *   The base URI for the reaction service's API endpoints.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, ReactionService[IO]]` that ensures the lifecycle of the `ReactionService` is managed correctly.
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
   * @param reactionService
   *   The reaction service used to provide dependencies for the `ReaktoroService`.
   * @param client
   *   The HTTP client instance used to make requests to the Reaktoro service endpoints.
   * @param baseUri
   *   The base URI for the Reaktoro service's API endpoints.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, ReaktoroService[IO]]` that ensures the lifecycle of the `ReaktoroService` is managed correctly.
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
   * Creates a managed resource for the `CacheService`.
   *
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, CacheService[IO]]` that ensures the lifecycle of the `CacheService` is managed correctly.
   */
  def cacheServiceResource(
    implicit logger: Logger[IO]
  ): Resource[IO, CacheService[IO]] =
    Resource.make(
      logger.info("Creating Cache Service") *>
        IO(new CacheService[IO])
    )(_ =>
      logger.info("Shutting down Cache Service").handleErrorWith(_ => IO.unit)
    )

  /**
   * Creates a managed resource for the `DistributedCacheService`.
   *
   * @param system
   *   The `ActorSystem` used for Akka-based concurrency and distributed data.
   * @param selfUniqueAddress
   *   The unique address of the current actor system instance, used for distributed data in a cluster.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, DistributedCacheService[IO]]` that ensures the lifecycle of the `DistributedCacheService` is
   *   managed correctly.
   */
  def distributedCacheServiceResource(
    system: ActorSystem,
    selfUniqueAddress: SelfUniqueAddress
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, DistributedCacheService[IO]] =
    Resource.make(
      logger.info("Creating Distributed Cache Service") *>
        IO(new DistributedCacheService[IO](system, selfUniqueAddress))
    )(_ =>
      logger.info("Shutting down Distributed Cache Service").handleErrorWith(_ => IO.unit)
    )

}
