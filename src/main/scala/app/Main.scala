package app

import api.{Endpoints, ServerBuilder}
import akka.actor.ActorSystem
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.comcast.ip4s.{Host, Port}
import core.services.{CacheService, MechanismService, ReactionService}
import config.ConfigLoader
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import scala.concurrent.ExecutionContext
import org.http4s.Uri
import config.ConfigLoader.httpClientConfig

object Main extends IOApp {

  def actorSystemResource(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    logger: Logger[IO]
  ): Resource[IO, ActorSystem] =
    Resource.make(
      logger.info("Creating Actor System") *> IO(system)
    )(system =>
      IO.fromFuture(IO(system.terminate())).attempt.flatMap {
        case Right(_) => logger.info("Actor system terminated successfully")
        case Left(ex) => logger.error(s"Actor system termination failed: ${ex.getMessage}")
      }
    )

  def serverBuilderResource(
    endpoints: Endpoints
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, ServerBuilder] =
    Resource.make(
      logger.info("Creating Server Builder") *> IO(new ServerBuilder(endpoints))
    )(endpoints =>
      logger.info("Shutting down ServerBuilder").handleErrorWith(_ => IO.unit)
    )

  def endpointsResource(
    reactionService: ReactionService[IO],
    mechanismService: MechanismService[IO]
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, Endpoints] =
    Resource.make(
      logger.info("Creating Endpoints") *> IO(new Endpoints(reactionService, mechanismService))
    )(endpoints =>
      logger.info("Shutting down Endpoints").handleErrorWith(_ => IO.unit)
    )

  def clientResource(
    implicit logger: Logger[IO]
  ): Resource[IO, Client[IO]] =
    EmberClientBuilder.default[IO].build

  def mechanismServiceResource(
    client: Client[IO],
    cacheService: CacheService[IO],
    baseUri: Uri
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, MechanismService[IO]] =
    Resource.make(
      logger.info("Creating Mechanism Service") *>
        IO(new MechanismService[IO](client, cacheService, baseUri))
    )(_ =>
      logger.info("Shutting down Mechanism Service").handleErrorWith(_ => IO.unit)
    )

  def reactionServiceResource(
    client: Client[IO],
    cacheService: CacheService[IO],
    baseUri: Uri
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, ReactionService[IO]] =
    Resource.make(
      logger.info("Creating Reaction Service") *>
        IO(new ReactionService[IO](client, cacheService, baseUri))
    )(_ =>
      logger.info("Shutting down Reaction Service").handleErrorWith(_ => IO.unit)
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
    host: Host,
    port: Port,
    baseUri: Uri
  )(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    logger: Logger[IO]
  ): Resource[IO, Unit] =
    for {
      system           <- actorSystemResource
      client           <- clientResource
      cacheService     <- cacheServiceResource
      mechanismService <- mechanismServiceResource(client, cacheService, baseUri / "mechanism")
      reactionService  <- reactionServiceResource(client, cacheService, baseUri / "reaction")
      endpoints        <- endpointsResource(reactionService, mechanismService)
      serverBuilder    <- serverBuilderResource(endpoints)
      server           <- serverBuilder.startServer(host, port)
      _                <- Resource.eval(logger.info("Press ENTER to terminate..."))
      _                <- Resource.eval(IO(scala.io.StdIn.readLine))
    } yield ()

  override def run(
    args: List[String]
  ): IO[ExitCode] = {
    val httpConfig       = ConfigLoader.httpConfig
    val httpClientConfig = ConfigLoader.httpClientConfig

    implicit val logger: Logger[IO]   = Slf4jLogger.getLogger[IO]
    implicit val system: ActorSystem  = ActorSystem("ChemistFlowActorSystem")
    implicit val ec: ExecutionContext = system.dispatcher

    runApp(httpConfig.host, httpConfig.port, httpClientConfig.baseUri).use(_ => IO.unit).as(ExitCode.Success)
  }

}
