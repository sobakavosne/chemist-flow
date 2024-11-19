package app

import akka.actor.ActorSystem
import akka.cluster.ddata.{DistributedData, SelfUniqueAddress}

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.toSemigroupKOps

import config.ConfigLoader
import config.ConfigLoader.DefaultConfigLoader

import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext

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
   * Runs the application, setting up all required resources.
   *
   * @param config
   *   The configuration loader used to provide application settings.
   * @param ec
   *   The `ExecutionContext` used for asynchronous operations.
   * @param system
   *   The `ActorSystem` used for managing Akka-based concurrency.
   * @param logger
   *   An implicit logger instance for logging lifecycle events and errors during resource creation and application
   *   runtime.
   * @return
   *   A `Resource[IO, Unit]` that encapsulates the full lifecycle of the application, ensuring that all resources are
   *   properly initialised and released.
   */
  private def runApp(
    config: ConfigLoader
  )(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    selfUniqueAddress: SelfUniqueAddress,
    logger: Logger[IO]
  ): Resource[IO, Unit] =
    val preprocessorBaseUri = config.preprocessorHttpClientConfig.baseUri
    val engineBaseUri       = config.engineHttpClientConfig.baseUri
    val host                = config.httpConfig.host
    val port                = config.httpConfig.port

    for {
      system                <- actorSystemResource
      client                <- clientResource
      cacheService          <- distributedCacheServiceResource
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

    runApp(config)
      .use(_ => IO.unit)
      .as(ExitCode.Success)
  }

}
