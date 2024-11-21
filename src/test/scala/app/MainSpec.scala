package app

import akka.actor.ActorSystem
import akka.cluster.ddata.{DistributedData, SelfUniqueAddress}

import api.endpoints.preprocessor.PreprocessorEndpoints
import api.ServerBuilder

import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import cats.implicits.toSemigroupKOps

import com.comcast.ip4s.{Host, Port}

import config.ConfigLoader.DefaultConfigLoader

import core.services.cache.DistributedCacheService
import core.services.flow.ReaktoroService
import core.services.preprocessor.{MechanismService, ReactionService}

import org.http4s.Uri
import org.http4s.ember.client.EmberClientBuilder
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class MainSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  implicit val logger: Logger[IO]                   = Slf4jLogger.getLogger[IO]
  implicit val system: ActorSystem                  = ActorSystem("TestSystem", DefaultConfigLoader.pureConfig)
  implicit val selfUniqueAddress: SelfUniqueAddress = DistributedData(system).selfUniqueAddress

  override def afterAll(): Unit = {
    system.terminate()
    Await
      .result(system.whenTerminated, 1.seconds)
      .asInstanceOf[Unit]
  }

  "Main" should {
    "start the http4s server as a Resource" in {

      val maybeHost       = Host.fromString("0.0.0.0").get
      val maybePort       = Port.fromInt(8081).get
      val preprocessorUri = Uri.unsafeFromString("http://localhost:8080")
      val engineUri       = Uri.unsafeFromString("http://localhost:8082/api")

      val serverResource = for {
        client                <- EmberClientBuilder.default[IO].build
        cacheService          <- Resource.make(
                                   IO(new DistributedCacheService[IO](system, selfUniqueAddress))
                                 )(_ => IO.unit)
        mechanismService      <- Resource.make(
                                   IO(new MechanismService[IO](cacheService, client, preprocessorUri / "mechanism"))
                                 )(_ => IO.unit)
        reactionService       <- Resource.make(
                                   IO(new ReactionService[IO](cacheService, client, preprocessorUri / "reaction"))
                                 )(_ => IO.unit)
        reaktoroService       <- Resource.make(
                                   IO(new ReaktoroService[IO](reactionService, client, engineUri / "reaction"))
                                 )(_ => IO.unit)
        preprocessorEndpoints <- Resource.make(
                                   IO(new PreprocessorEndpoints(reactionService, mechanismService))
                                 )(_ => IO.unit)
        reaktoroEndpoints     <- Resource.make(
                                   IO(new PreprocessorEndpoints(reactionService, mechanismService))
                                 )(_ => IO.unit)
        serverBuilder         <- Resource.make(
                                   IO(new ServerBuilder(preprocessorEndpoints.routes <+> reaktoroEndpoints.routes))
                                 )(_ => IO.unit)
        server                <- serverBuilder.startServer(maybeHost, maybePort)
      } yield server

      serverResource
        .use { server =>
          IO {
            server.address.getPort shouldEqual 8081
            // Additional tests can verify the availability and behaviour of endpoints.
          }
        }
        .unsafeToFuture()
    }
  }
}
