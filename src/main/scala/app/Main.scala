package app

import api.ServerBuilder
import api.endpoints.preprocessor.PreprocessorEndpoints

import akka.actor.ActorSystem

import cats.effect.{ExitCode, IO, IOApp, Resource}

import core.services.cache.CacheService
import core.services.flow.ReaktoroService
import core.services.preprocessor.{MechanismService, ReactionService}

import config.ConfigLoader
import config.ConfigLoader.DefaultConfigLoader

import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.Uri

import scala.concurrent.ExecutionContext

object Main extends IOApp {

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

  def serverBuilderResource(
    preprocessorEndpoints: PreprocessorEndpoints
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, ServerBuilder] =
    Resource.make(
      logger.info("Creating Server Builder") *>
        IO(new ServerBuilder(preprocessorEndpoints))
    )(endpoints =>
      logger.info("Shutting down ServerBuilder").handleErrorWith(_ => IO.unit)
    )

  def endpointsResource(
    reactionService: ReactionService[IO],
    mechanismService: MechanismService[IO],
    reaktoroService: ReaktoroService[IO]
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, PreprocessorEndpoints] =
    Resource.make(
      logger.info("Creating Endpoints") *>
        IO(new PreprocessorEndpoints(reactionService, mechanismService))
    )(endpoints =>
      logger.info("Shutting down Endpoints").handleErrorWith(_ => IO.unit)
    )

  def clientResource(
    implicit logger: Logger[IO]
  ): Resource[IO, Client[IO]] =
    EmberClientBuilder.default[IO].build

  def mechanismServiceResource(
    cacheService: CacheService[IO],
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

  def reactionServiceResource(
    cacheService: CacheService[IO],
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

  def cacheServiceResource(
    implicit logger: Logger[IO]
  ): Resource[IO, CacheService[IO]] =
    Resource.make(
      logger.info("Creating Cache Service") *> IO(new CacheService[IO])
    )(_ =>
      logger.info("Shutting down Cache Service").handleErrorWith(_ => IO.unit)
    )

  def runApp(
    config: ConfigLoader
  )(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    logger: Logger[IO]
  ): Resource[IO, Unit] =
    val preprocessorBaseUri = config.preprocessorHttpClientConfig.baseUri
    val engineBaseUri       = config.engineHttpClientConfig.baseUri
    val host                = config.httpConfig.host
    val port                = config.httpConfig.port

    for {
      system           <- actorSystemResource
      client           <- clientResource
      cacheService     <- cacheServiceResource
      mechanismService <- mechanismServiceResource(cacheService, client, preprocessorBaseUri / "mechanism")
      reactionService  <- reactionServiceResource(cacheService, client, preprocessorBaseUri / "reaction")
      reaktoroService  <- reaktoroServiceResource(reactionService, client, engineBaseUri / "reaction")
      endpoints        <- endpointsResource(reactionService, mechanismService, reaktoroService)
      serverBuilder    <- serverBuilderResource(endpoints)
      server           <- serverBuilder.startServer(host, port)
      _                <- Resource.eval(logger.info("Press ENTER to terminate..."))
      _                <- Resource.eval(IO(scala.io.StdIn.readLine))
    } yield ()

  override def run(
    args: List[String]
  ): IO[ExitCode] = {
    val config = DefaultConfigLoader

    implicit val logger: Logger[IO]   = Slf4jLogger.getLogger[IO]
    implicit val system: ActorSystem  = ActorSystem("ChemistFlowActorSystem")
    implicit val ec: ExecutionContext = system.dispatcher

    runApp(config)
      .use(_ => IO.unit)
      .as(ExitCode.Success)
  }

}
