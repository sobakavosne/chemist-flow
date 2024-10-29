package app

import akka.actor.ActorSystem
import api.Endpoints
import cats.effect.{ExitCode, IO, IOApp, Resource}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends IOApp {
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
            case Success(_) =>
              println("Actor system terminated successfully")
            case Failure(ex) =>
              println(s"Actor system termination failed: ${ex.getMessage}")
          }
      ).handleErrorWith(ex => IO(println(s"Failed to terminate actor system: ${ex.getMessage}")))
    )

  def endpointsResource: Resource[IO, Endpoints] =
    Resource.make(IO(new Endpoints))(endpoints =>
      IO(println("Shutting down Endpoints")).handleErrorWith(_ => IO.unit)
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
      system    <- actorSystemResource
      endpoints <- endpointsResource
      _         <- endpoints.startServer(host, port)(system)
      _         <- Resource.eval(IO(scala.io.StdIn.readLine))
    } yield ()

  override def run(
    args: List[String]
  ): IO[ExitCode] = {
    implicit val system: ActorSystem  = ActorSystem("ChemistFlowActorSystem")
    implicit val ec: ExecutionContext = system.dispatcher
    val host: String                  = sys.env.getOrElse("CHEMIST_FLOW_HOST", "0.0.0.0")
    val port: Int                     = sys.env.getOrElse("CHEMIST_FLOW_PORT", "8081").toInt

    runApp(host, port).use(_ => IO.unit).as(ExitCode.Success)
  }
}
