package app

import akka.actor.ActorSystem
import akka.cluster.ddata.{DistributedData, SelfUniqueAddress}
import akka.util.Timeout

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.toSemigroupKOps

import config.ConfigLoader
import config.ConfigLoader.DefaultConfigLoader

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
 * Entry point for the application. Responsible for configuring and starting all resources and services.
 */
object Main extends IOApp {

  /**
   * Configures and manages the lifecycle of all resources and services required for the application.
   *
   * This method initialises resources such as the actor system, HTTP clients, distributed cache, services, and server.
   * It ensures proper resource management by leveraging `Resource` for initialisation and cleanup.
   *
   * @param config
   *   The configuration loader that provides application-specific settings, such as HTTP endpoints, client
   *   configurations, and server properties.
   * @param ec
   *   The `ExecutionContext` used to handle asynchronous operations across the application.
   * @param system
   *   The `ActorSystem` used for Akka-based concurrency and distributed data.
   * @param selfUniqueAddress
   *   The unique address of the current actor system, used for distributed data in a cluster.
   * @param logger
   *   An implicit logger instance for recording lifecycle events, debugging, and error handling.
   * @return
   *   A `Resource[IO, Unit]` that encapsulates the application's full lifecycle. This includes initialisation, running
   *   the server, and ensuring all resources are properly cleaned up when the application terminates.
   */
  private def runApp(
    config: ConfigLoader
  )(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    selfUniqueAddress: SelfUniqueAddress,
    cacheExpiration: Timeout,
    logger: Logger[IO]
  ): Resource[IO, Unit] =
    val preprocessorBaseUri = config.preprocessorHttpClientConfig.baseUri
    val engineBaseUri       = config.engineHttpClientConfig.baseUri
    val host                = config.httpConfig.host
    val port                = config.httpConfig.port

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
    implicit val cacheExpiration: Timeout             = Timeout(5.minutes)

    runApp(config)
      .use(_ => IO.unit)
      .as(ExitCode.Success)
  }

}
