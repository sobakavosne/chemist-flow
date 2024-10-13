package app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import api.Endpoints
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class MainSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {

  implicit val system: ActorSystem        = ActorSystem("TestActorSystem")
  implicit val materializer: Materializer = Materializer(system)

  "Main" should {
    "start the ActorSystem and HTTP server" in {
      val endpoints = new Endpoints()
      val bindingFuture: Future[Http.ServerBinding] =
        endpoints.startServer("localhost", 8080)

      val binding = Await.result(bindingFuture, 5.seconds)

      binding.localAddress.getPort shouldEqual 8080

      // More tests here to verify specific endpoint behavior
    }
  }

  override def afterAll(): Unit = {
    system.terminate()
    Await.result(system.whenTerminated, 10.seconds)
  }
}
