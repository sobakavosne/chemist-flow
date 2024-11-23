package app

import akka.actor.ActorSystem
import akka.cluster.ddata.{DistributedData, SelfUniqueAddress}
import akka.util.Timeout

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.toSemigroupKOps

import com.comcast.ip4s.Host
import com.comcast.ip4s.Port

import config.ConfigLoader
import config.ConfigLoader.DefaultConfigLoader

import org.http4s.Uri
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

import app.units.SystemResources.actorSystemResource
import app.units.ClientResources.clientResource
import app.units.EndpointResources.{preprocessorEndpointsResource, reaktoroEndpointsResource}
import app.units.ServerResources.serverBuilderResource
import app.units.ServiceResources.{
  distributedCacheServiceResource,
  mechanismServiceResource,
  reactionServiceResource,
  reaktoroServiceResource
}

/**
 * Main entry point for the application.
 *
 * This object configures and starts all required resources and services for the application, including:
 *   - Actor system setup for Akka-based concurrency.
 *   - HTTP client setup for external API interactions.
 *   - Distributed cache management with a configurable time-to-live (TTL).
 *   - Initialisation of core services (`MechanismService`, `ReactionService`, `ReaktoroService`).
 *   - HTTP server setup for serving API endpoints.
 *
 * Proper lifecycle management is ensured using `cats.effect.Resource`, which guarantees that all resources are
 * initialised and cleaned up correctly. This entry point waits for user input to terminate the application, ensuring a
 * controlled shutdown.
 */
object Main extends IOApp {

  /**
   * Configures and manages the lifecycle of all resources and services required for the application.
   *
   * This method integrates the initialisation and cleanup of:
   *   - Actor system for concurrency and distributed data.
   *   - HTTP clients for API communication.
   *   - Distributed cache services with a configurable time-to-live (TTL) for cache expiration.
   *   - Core application services (`MechanismService`, `ReactionService`, `ReaktoroService`).
   *   - HTTP server for hosting API endpoints. API routes are combined and served as a single HTTP application.
   *
   * The method waits for user input to terminate the application, ensuring a controlled shutdown. All resources are
   * composed using `cats.effect.Resource`, ensuring proper cleanup on termination.
   *
   * @param config
   *   The configuration loader for application-specific settings.
   * @param ec
   *   The `ExecutionContext` for handling asynchronous operations.
   * @param system
   *   The Akka `ActorSystem` for managing concurrency.
   * @param selfUniqueAddress
   *   The unique address of the current actor system, used for distributed data.
   * @param ttl
   *   A timeout representing the time-to-live (TTL) for cache expiration.
   * @param logger
   *   An implicit logger instance for lifecycle logging.
   * @return
   *   A `Resource[IO, Unit]` that encapsulates the entire lifecycle of the application.
   */
  private def runApp(
    config: ConfigLoader
  )(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    selfUniqueAddress: SelfUniqueAddress,
    ttl: Timeout,
    logger: Logger[IO]
  ): Resource[IO, Unit] =
    val preprocessorBaseUri: Uri = config.preprocessorHttpClientConfig.baseUri
    val engineBaseUri: Uri       = config.engineHttpClientConfig.baseUri
    val host: Host               = config.httpConfig.host
    val port: Port               = config.httpConfig.port

    for {
      system                <- actorSystemResource
      client                <- clientResource
      cacheService          <- distributedCacheServiceResource(system, selfUniqueAddress)
      mechanismService      <- mechanismServiceResource(cacheService, client, preprocessorBaseUri / "mechanism")
      reactionService       <- reactionServiceResource(cacheService, client, preprocessorBaseUri / "reaction")
      reaktoroService       <- reaktoroServiceResource(reactionService, client, engineBaseUri)
      preprocessorEndpoints <- preprocessorEndpointsResource(reactionService, mechanismService)
      reaktoroEndpoints     <- reaktoroEndpointsResource(reaktoroService)
      serverBuilder         <- serverBuilderResource(preprocessorEndpoints.routes <+> reaktoroEndpoints.routes)
      server                <- serverBuilder.startServer(host, port)
      _                     <- Resource.eval(logger.info("Press ENTER to terminate..."))
      _                     <- Resource.eval(IO(scala.io.StdIn.readLine))
    } yield ()

  /**
   * Main entry point for the application.
   *
   * This method sets up the application by:
   *   - Initialising implicit dependencies, including the logger, actor system, execution context, distributed data,
   *     unique address, and cache expiration timeout (TTL).
   *   - Loading configuration using the `DefaultConfigLoader`.
   *   - Running the application using `runApp` and ensuring all resources are cleaned up on exit.
   *
   * @param args
   *   The command-line arguments passed to the application.
   * @return
   *   An `IO[ExitCode]` indicating the application's final exit code upon completion.
   */
  override def run(
    args: List[String]
  ): IO[ExitCode] = {
    val config = DefaultConfigLoader

    implicit val logger: Logger[IO]                   = Slf4jLogger.getLogger[IO]
    implicit val system: ActorSystem                  = ActorSystem("ChemistFlowActorSystem", config.pureConfig)
    implicit val ec: ExecutionContext                 = system.dispatcher
    implicit val distributedData                      = DistributedData(system)
    implicit val selfUniqueAddress: SelfUniqueAddress = distributedData.selfUniqueAddress
    implicit val ttl: Timeout                         = Timeout(5.minutes)

    runApp(config)
      .use(_ => IO.unit)
      .as(ExitCode.Success)
  }

}
