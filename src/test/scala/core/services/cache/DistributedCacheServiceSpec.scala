import akka.actor.ActorSystem
import akka.cluster.ddata.{DistributedData, SelfUniqueAddress}
import akka.testkit.TestKit
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import config.ConfigLoader.DefaultConfigLoader
import core.domain.preprocessor.{Mechanism, MechanismId, Reaction, ReactionId}
import core.services.cache.DistributedCacheService
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.concurrent.duration.DurationInt

class DistributedCacheServiceTest extends AnyFunSuite with Matchers with BeforeAndAfterAll {

  implicit val system: ActorSystem                  = ActorSystem("TestSystem", DefaultConfigLoader.pureConfig)
  implicit val selfUniqueAddress: SelfUniqueAddress = DistributedData(system).selfUniqueAddress

  val cacheService = new DistributedCacheService[IO]

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  test("putMechanism and getMechanism should store and retrieve a mechanism") {
    val mechanismId: MechanismId = 1
    val mechanism: Mechanism     = Mechanism(mechanismId, "mechanism", "type", 1.0)

    val result = for {
      _         <- cacheService.putMechanism(mechanismId, mechanism)
      _         <- IO.sleep(2.seconds)
      retrieved <- cacheService.getMechanism(mechanismId)
    } yield retrieved

    val retrievedMechanism = result.unsafeRunSync()

    retrievedMechanism shouldEqual Some(mechanism)
  }

  test("putReaction and getReaction should store and retrieve a reaction") {
    val reactionId: ReactionId = 1
    val reaction: Reaction     = Reaction(reactionId, "reaction")

    val result = for {
      _         <- cacheService.putReaction(reactionId, reaction)
      _         <- IO.sleep(2.seconds)
      retrieved <- cacheService.getReaction(reactionId)
    } yield retrieved

    val retrievedReaction = result.unsafeRunSync()

    retrievedReaction shouldEqual Some(reaction)
  }
}
