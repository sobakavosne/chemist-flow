package app

import api.Endpoints
import api.ServerBuilder
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class MainSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  "Main" should {
    "start the http4s server as a Resource" in {
      implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
      implicit val endpoints          = new Endpoints
      val serverBuilder               = new ServerBuilder
      val maybeHost                   = Host.fromString("0.0.0.0")
      val maybePort                   = Port.fromInt(8081)
      val bindingResource             = serverBuilder.startServer(maybeHost.get, maybePort.get)

      bindingResource
        .use { server =>
          IO {
            server.address.getPort shouldEqual 8081
            // More tests here to verify specific endpoint behaviour
          }
        }
        .unsafeToFuture()
    }
  }
}
