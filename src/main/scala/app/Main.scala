package app

import akka.actor.ActorSystem
import api.Endpoints
import akka.stream.Materializer

object Main extends App {
  implicit val system: ActorSystem        = ActorSystem("ChemistActorSystem")
  implicit val materializer: Materializer = Materializer(system)

  val host = sys.env.getOrElse("CHEMIST_FLOW_HOST", "0.0.0.0")
  val port = sys.env.getOrElse("CHEMIST_FLOW_PORT", "8081").toInt

  val endpoints = new Endpoints()
  endpoints.startServer(host, port)

  scala.io.StdIn.readLine()

  sys.addShutdownHook {
    system.terminate()
  }
}
