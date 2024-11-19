package app.units

import cats.effect.{IO, Resource}

import core.services.cache.CacheService
import core.services.cache.DistributedCacheService
import core.services.preprocessor.{MechanismService, ReactionService}
import core.services.flow.ReaktoroService

import org.http4s.client.Client
import org.http4s.Uri
import org.typelevel.log4cats.Logger
import akka.actor.ActorSystem
import akka.cluster.ddata.SelfUniqueAddress

/**
 * Provides resources for service initialisation and lifecycle management.
 */
object ServiceResources {

  /**
   * Creates a managed resource for the `MechanismService`.
   *
   * @param cacheService
   *   The `CacheService` instance used for caching mechanisms.
   * @param client
   *   The `Client[IO]` instance used for HTTP requests.
   * @param baseUri
   *   The base `Uri` for the mechanism service endpoints.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, MechanismService[IO]]` that manages the lifecycle of the `MechanismService` instance.
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
   *   The `CacheService` instance used for caching reactions.
   * @param client
   *   The `Client[IO]` instance used for HTTP requests.
   * @param baseUri
   *   The base `Uri` for the reaction service endpoints.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, ReactionService[IO]]` that manages the lifecycle of the `ReactionService` instance.
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
   *   The `ReactionService` instance used as a dependency for the `ReaktoroService`.
   * @param client
   *   The `Client[IO]` instance used for HTTP requests.
   * @param baseUri
   *   The base `Uri` for the Reaktoro service endpoints.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, ReaktoroService[IO]]` that manages the lifecycle of the `ReaktoroService` instance.
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
   *   A `Resource[IO, CacheService[IO]]` that manages the lifecycle of the `CacheService` instance.
   */
  def cacheServiceResource(
    implicit logger: Logger[IO]
  ): Resource[IO, CacheService[IO]] =
    Resource.make(
      logger.info("Creating Cache Service") *> IO(new CacheService[IO])
    )(_ =>
      logger.info("Shutting down Cache Service").handleErrorWith(_ => IO.unit)
    )

  /**
   * Creates a managed resource for the `DistributedCacheService`.
   *
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, DistributedCacheService[IO]]` that manages the lifecycle of the `CacheService` instance.
   */
  def distributedCacheServiceResource(
    implicit
    logger: Logger[IO],
    system: ActorSystem,
    selfUniqueAddress: SelfUniqueAddress
  ): Resource[IO, DistributedCacheService[IO]] =
    Resource.make(
      logger.info("Creating Cache Service") *> IO(new DistributedCacheService[IO])
    )(_ =>
      logger.info("Shutting down Cache Service").handleErrorWith(_ => IO.unit)
    )

}
