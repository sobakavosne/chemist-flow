package app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import api.Endpoints
import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class MainSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem("TestActorSystem")

  "Main" should {
    "start the ActorSystem and HTTP server as a Resource" in {
      val endpoints: Endpoints                              = new Endpoints()
      val bindingResource: Resource[IO, Http.ServerBinding] = endpoints.startServer("0.0.0.0", 8081)

      bindingResource
        .use { binding =>
          IO {
            binding.localAddress.getPort shouldEqual 8081

            // More tests here to verify specific endpoint behavior
          }
        }
        .unsafeToFuture()
    }
  }

  override def afterAll(): Unit = {
    system.terminate()
  }
}
