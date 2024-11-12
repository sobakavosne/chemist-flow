package app

import api.{Endpoints, ServerBuilder}
import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import com.comcast.ip4s.{Host, Port}
import core.services.{CacheService, MechanismService, ReactionService}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.Uri
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

class MainSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  "Main" should {
    "start the http4s server as a Resource" in {

      val maybeHost = Host.fromString("0.0.0.0").get
      val maybePort = Port.fromInt(8081).get
      val baseUri   = Uri.unsafeFromString("http://localhost:8081")

      val serverResource = for {
        client           <- EmberClientBuilder.default[IO].build
        cacheService     <- Resource.make(
                              IO(new CacheService[IO])
                            )(_ => IO.unit)
        mechanismService <- Resource.make(
                              IO(new MechanismService[IO](client, cacheService, baseUri / "mechanism"))
                            )(_ => IO.unit)
        reactionService  <- Resource.make(
                              IO(new ReactionService[IO](client, cacheService, baseUri / "reaction"))
                            )(_ => IO.unit)
        endpoints        <- Resource.make(
                              IO(new Endpoints(reactionService, mechanismService))
                            )(_ => IO.unit)
        serverBuilder    <- Resource.make(
                              IO(new ServerBuilder(endpoints))
                            )(_ => IO.unit)
        server           <- serverBuilder.startServer(maybeHost, maybePort)
      } yield server

      serverResource
        .use { server =>
          IO {
            server.address.getPort shouldEqual 8081
            // Additional tests can be added here to verify endpoint behaviour
          }
        }
        .unsafeToFuture()
    }
  }
}
