package app

import akka.actor.ActorSystem
import api.Endpoints
import cats.effect.{ExitCode, IO, IOApp, Resource}
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

  def endpointsResource: Resource[IO, Endpoints] =
    Resource.make(IO(new Endpoints))(endpoints =>
      IO(logger.info("Shutting down Endpoints")).handleErrorWith(_ => IO.unit)
    )

  def runApp(
    host: String,
    port: Int
  )(
    implicit
    ec: ExecutionContext,
    system: ActorSystem
  ): Resource[IO, Unit] =
    for {
      _         <- Resource.eval(IO(logger.info("Creating Actor system resource")))
      system    <- actorSystemResource
      _         <- Resource.eval(IO(logger.info("Creating Endpoints resource")))
      endpoints <- endpointsResource
      _         <- endpoints.startServer(host, port)(system)
      _         <- Resource.eval(IO(scala.io.StdIn.readLine))
    } yield ()

  override def run(
    args: List[String]
  ): IO[ExitCode] = {
    implicit val system: ActorSystem  = ActorSystem("ChemistFlowActorSystem")
    implicit val ec: ExecutionContext = system.dispatcher

    val httpConfig = ConfigLoader.httpConfig

    runApp(httpConfig.host, httpConfig.port).use(_ => IO.unit).as(ExitCode.Success)
  }
}
