package app.units

import akka.actor.ActorSystem

import cats.effect.{IO, Resource}

import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext

/**
 * Provides resources for managing system-level components, such as the `ActorSystem`.
 */
object SystemResources {

  /**
   * Creates a managed resource for the `ActorSystem`.
   *
   * @param ec
   *   The `ExecutionContext` to be used by the `ActorSystem`.
   * @param system
   *   The `ActorSystem` instance to be managed.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource[IO, ActorSystem]` that manages the lifecycle of the `ActorSystem` instance, ensuring proper
   *   termination.
   */
  def actorSystemResource(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    logger: Logger[IO]
  ): Resource[IO, ActorSystem] =
    Resource.make(
      logger.info("Creating Actor System") *>
        IO(system)
    )(system =>
      IO.fromFuture(IO(system.terminate())).attempt.flatMap {
        case Right(_) => logger.info("Actor system terminated successfully")
        case Left(ex) => logger.error(s"Actor system termination failed: ${ex.getMessage}")
      }
    )

}
