package app

import akka.actor.ActorSystem
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.comcast.ip4s.{Host, Port}
import api.{Endpoints, ServerBuilder}
import config.ConfigLoader
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import scala.concurrent.ExecutionContext

object Main extends IOApp {
  def actorSystemResource(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    logger: Logger[IO]
  ): Resource[IO, ActorSystem] =
    Resource.make(IO(system)) { system =>
      IO.fromFuture(IO(system.terminate())).attempt.flatMap {
        case Right(_) => logger.info("Actor system terminated successfully")
        case Left(ex) => logger.error(s"Actor system termination failed: ${ex.getMessage}")
      }
    }

  def serverBuilderResource(
    implicit
    endpoints: Endpoints,
    serverBuilder: ServerBuilder,
    logger: Logger[IO]
  ): Resource[IO, ServerBuilder] =
    Resource.make(IO(serverBuilder))(endpoints =>
      logger.info("Shutting down ServerBuilder").handleErrorWith(_ => IO.unit)
    )

  def runApp(
    host: Host,
    port: Port
  )(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    endpoints: Endpoints,
    serverBuilder: ServerBuilder,
    logger: Logger[IO]
  ): Resource[IO, Unit] =
    for {
      _             <- Resource.eval(logger.info("Creating Actor system resource"))
      system        <- actorSystemResource
      _             <- Resource.eval(logger.info("Creating ServerBuilder resource"))
      serverBuilder <- serverBuilderResource
      _             <- serverBuilder.startServer(host, port)
      _             <- Resource.eval(logger.info("Press ENTER to terminate..."))
      _             <- Resource.eval(IO(scala.io.StdIn.readLine))
    } yield ()

  override def run(
    args: List[String]
  ): IO[ExitCode] = {
    val httpConfig = ConfigLoader.httpConfig

    implicit val logger: Logger[IO]           = Slf4jLogger.getLogger[IO]
    implicit val endpoints: Endpoints         = new Endpoints
    implicit val serverBuilder: ServerBuilder = new ServerBuilder
    implicit val system: ActorSystem          = ActorSystem("ChemistFlowActorSystem")
    implicit val ec: ExecutionContext         = system.dispatcher

    runApp(httpConfig.host, httpConfig.port).use(_ => IO.unit).as(ExitCode.Success)
  }
}
