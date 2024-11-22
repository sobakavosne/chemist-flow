package app.units

import akka.actor.ActorSystem

import cats.effect.{IO, Resource}

import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext

/**
 * Provides managed resources for system-level components.
 *
 * This object encapsulates the lifecycle management of system-level components like the `ActorSystem`, ensuring proper
 * initialisation and termination using the `Resource` abstraction.
 */
object SystemResources {

  /**
   * Creates a managed resource for the `ActorSystem`.
   *
   * This method manages the lifecycle of an `ActorSystem` instance, ensuring it is properly initialised and terminated.
   * Lifecycle events, including creation and termination, are logged for observability. Any errors during termination
   * are captured and logged.
   *
   * Example usage:
   * {{{
   *   implicit val system: ActorSystem = ActorSystem("my-system")
   *   implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
   *   implicit val ec: ExecutionContext = system.dispatcher
   *
   *   val systemResource = SystemResources.actorSystemResource
   *   systemResource.use { actorSystem =>
   *     // Use the actor system
   *   }
   * }}}
   *
   * @param ec
   *   The `ExecutionContext` to be used by the `ActorSystem`.
   * @param system
   *   The `ActorSystem` instance to be managed.
   * @param logger
   *   An implicit logger instance for logging lifecycle events.
   * @return
   *   A `Resource[IO, ActorSystem]` that ensures proper initialisation and termination of the `ActorSystem`.
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
