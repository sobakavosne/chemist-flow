package app

import akka.actor.ActorSystem
import api.Endpoints
import akka.stream.Materializer

object Main extends App {
  implicit val system: ActorSystem        = ActorSystem("ChemistActorSystem")
  implicit val materializer: Materializer = Materializer(system)

  val endpoints = new Endpoints()
  endpoints.startServer("localhost", 8080)

  println("Press ENTER to exit...")
  scala.io.StdIn.readLine()

  system.terminate()
}
