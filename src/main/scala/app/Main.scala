package app

import akka.actor.ActorSystem
import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.comcast.ip4s.{Host, Port}
import resource.api.{Endpoints, ServerBuilder}
import resource.core.util.ConfigLoader
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends IOApp {
  private val logger = LoggerFactory.getLogger(getClass)

  def actorSystemResource(
    implicit
    ec: ExecutionContext,
    system: ActorSystem
  ): Resource[IO, ActorSystem] =
    Resource.make(IO(system))(system =>
      IO(
        system
          .terminate()
          .onComplete {
            case Success(_)  => logger.info("Actor system terminated successfully")
            case Failure(ex) => logger.error(s"Actor system termination failed: ${ex.getMessage}")
          }
      ).handleErrorWith(ex =>
        IO(logger.error(s"Failed to terminate actor system: ${ex.getMessage}"))
      )
    )

  def serverBuilderResource(
    implicit endpoints: Endpoints
  ): Resource[IO, ServerBuilder] =
    Resource.make(IO(new ServerBuilder))(endpoints =>
      IO(logger.info("Shutting down ServerBuilder")).handleErrorWith(_ => IO.unit)
    )

  def runApp(
    host: Host,
    port: Port
  )(
    implicit
    ec: ExecutionContext,
    system: ActorSystem,
    endpoints: Endpoints
  ): Resource[IO, Unit] =
    for {
      _             <- Resource.eval(IO(logger.info("Creating Actor system resource")))
      system        <- actorSystemResource
      _             <- Resource.eval(IO(logger.info("Creating ServerBuilder resource")))
      serverBuilder <- serverBuilderResource
      _             <- serverBuilder.startServer(host, port)
      _             <- Resource.eval(IO(scala.io.StdIn.readLine))
    } yield ()

  override def run(
    args: List[String]
  ): IO[ExitCode] = {
    implicit val endpoints: Endpoints = new Endpoints
    implicit val system: ActorSystem  = ActorSystem("ChemistFlowActorSystem")
    implicit val ec: ExecutionContext = system.dispatcher

    val httpConfig = ConfigLoader.httpConfig

    runApp(httpConfig.host, httpConfig.port).use(_ => IO.unit).as(ExitCode.Success)
  }
}
