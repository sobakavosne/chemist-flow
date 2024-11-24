package core.services.cache

import akka.actor.ActorSystem
import akka.cluster.{Cluster, MemberStatus}
import akka.cluster.ddata.{DistributedData, SelfUniqueAddress}
import akka.testkit.TestKit
import akka.util.Timeout

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import com.typesafe.config.Config

import config.TestConfigLoader.DefaultConfigLoader

import core.domain.preprocessor.{
  FOLLOW,
  Mechanism,
  MechanismDetails,
  MechanismId,
  Reaction,
  ReactionDetails,
  ReactionId,
  Stage
}

import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import java.util.concurrent.TimeUnit

class DistributedCacheServiceSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {

  val config: Config                       = DefaultConfigLoader.pureConfig
  val system: ActorSystem                  = ActorSystem("TestChemistFlowActorSystem", config)
  val selfUniqueAddress: SelfUniqueAddress = DistributedData(system).selfUniqueAddress
  val cluster: Cluster                     = Cluster(system)

  implicit val ec: ExecutionContext                    = system.dispatcher
  implicit val ttlDistributed: Timeout                 = Timeout(1.seconds)
  implicit val localTtlWithUnit: Tuple2[Int, TimeUnit] = (5, TimeUnit.MINUTES)

  cluster.registerOnMemberUp {
    println("Cluster is fully operational")
  }

  def ensureClusterIsReady(): Unit = {
    val timeout = System.currentTimeMillis() + 1000
    while (System.currentTimeMillis() < timeout && !cluster.state.members.exists(_.status == MemberStatus.Up)) {
      Thread.sleep(100)
    }
    if (!cluster.state.members.exists(_.status == MemberStatus.Up)) {
      throw new RuntimeException("Cluster did not stabilise")
    }
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  override protected def beforeAll(): Unit = {
    ensureClusterIsReady()
  }

  "DistributedCacheService" should {

    "store and retrieve a mechanism using putMechanism and getMechanism" in {
      val cacheService: DistributedCacheService[IO] = new DistributedCacheService[IO](system, selfUniqueAddress)
      val mechanismId: MechanismId                  = 1
      val mechanism: Mechanism                      = Mechanism(mechanismId, "mechanism", "type", 1.0)

      val result = for {
        _         <- cacheService.putMechanism(mechanismId, mechanism)
        retrieved <- retryIO(10, 500.millis) {
                       cacheService.getMechanism(mechanismId).flatMap {
                         case Some(value) => IO.pure(value)
                         case None        => IO.raiseError(new Exception("Mechanism not yet replicated"))
                       }
                     }
      } yield retrieved

      val retrievedMechanism = result.unsafeRunSync()

      retrievedMechanism shouldEqual mechanism
    }

    "store and retrieve mechanism details using putMechanismDetails and getMechanismDetails" in {
      val cacheService: DistributedCacheService[IO] = new DistributedCacheService[IO](system, selfUniqueAddress)
      val mechanismId: MechanismId                  = 1
      val mechanism: Mechanism                      = Mechanism(mechanismId, "mechanism", "type", 1.0)
      val mechanismDetails: MechanismDetails        = MechanismDetails(
        mechanismContext  = (mechanism, FOLLOW("description")),
        stageInteractants = List((Stage(10, "name", "description", List()), List()))
      )

      val result = for {
        _         <- cacheService.putMechanismDetails(mechanismId, mechanismDetails)
        retrieved <- retryIO(10, 500.millis) {
                       cacheService.getMechanismDetails(mechanismId).flatMap {
                         case Some(value) => IO.pure(value)
                         case None        => IO.raiseError(new Exception("Mechanism details not yet replicated"))
                       }
                     }
      } yield retrieved

      val retrievedMechanismDetails = result.unsafeRunSync()

      retrievedMechanismDetails shouldEqual mechanismDetails
    }

    "store and retrieve a reaction using putReaction and getReaction" in {
      val cacheService: DistributedCacheService[IO] = new DistributedCacheService[IO](system, selfUniqueAddress)
      val reactionId: ReactionId                    = 1
      val reaction: Reaction                        = Reaction(reactionId, "reaction")

      val result = for {
        _         <- cacheService.putReaction(reactionId, reaction)
        retrieved <- retryIO(10, 500.millis) {
                       cacheService.getReaction(reactionId).flatMap {
                         case Some(value) => IO.pure(value)
                         case None        => IO.raiseError(new Exception("Reaction not yet replicated"))
                       }
                     }
      } yield retrieved

      val retrievedReaction = result.unsafeRunSync()

      retrievedReaction shouldEqual reaction
    }

    "store and retrieve reaction details using putReactionDetails and getReactionDetails" in {
      val cacheService: DistributedCacheService[IO] = new DistributedCacheService[IO](system, selfUniqueAddress)
      val reactionId: ReactionId                    = 1
      val reaction: Reaction                        = Reaction(reactionId, "reaction")
      val reactionDetails: ReactionDetails          = ReactionDetails(reaction, List(), List(), List())

      val result = for {
        _         <- cacheService.putReactionDetails(reactionId, reactionDetails)
        retrieved <- retryIO(10, 500.millis) {
                       cacheService.getReactionDetails(reactionId).flatMap {
                         case Some(value) => IO.pure(value)
                         case None        => IO.raiseError(new Exception("Reaction details not yet replicated"))
                       }
                     }
      } yield retrieved

      val retrievedReactionDetails = result.unsafeRunSync()

      retrievedReactionDetails shouldEqual reactionDetails
    }

    "create a mechanism only if it doesn't exist using createMechanism" in {
      val cacheService: DistributedCacheService[IO] = new DistributedCacheService[IO](system, selfUniqueAddress)
      val mechanismId: MechanismId                  = 2
      val mechanism: Mechanism                      = Mechanism(mechanismId, "new-mechanism", "type", 1.0)

      val result = for {
        created <- cacheService.createMechanism(mechanismId, mechanism)
        attempt <- cacheService.createMechanism(mechanismId, mechanism)
      } yield (created, attempt)

      val (created, attempt) = result.unsafeRunSync()

      created shouldEqual Right(mechanism)
      attempt shouldEqual Left(s"Mechanism with ID $mechanismId already exists in the cache.")
    }

    "create a reaction only if it doesn't exist using createReaction" in {
      val cacheService: DistributedCacheService[IO] = new DistributedCacheService[IO](system, selfUniqueAddress)
      val reactionId: ReactionId                    = 2
      val reaction: Reaction                        = Reaction(reactionId, "new-reaction")

      val result = for {
        created <- cacheService.createReaction(reactionId, reaction)
        attempt <- cacheService.createReaction(reactionId, reaction)
      } yield (created, attempt)

      val (created, attempt) = result.unsafeRunSync()

      created shouldEqual Right(reaction)
      attempt shouldEqual Left(s"Reaction with ID $reactionId already exists in the cache.")
    }

    "return None for non-existent mechanism IDs" in {
      val cacheService: DistributedCacheService[IO] = new DistributedCacheService[IO](system, selfUniqueAddress)
      val result: Option[Mechanism]                 = cacheService.getMechanism(999).unsafeRunSync()
      result shouldBe None
    }

    "return None for non-existent reaction IDs" in {
      val cacheService             = new DistributedCacheService[IO](system, selfUniqueAddress)
      val result: Option[Reaction] = cacheService.getReaction(999).unsafeRunSync()

      result shouldBe None
    }
  }

  private def retryIO[A](retries: Int, delay: FiniteDuration)(action: IO[A]): IO[A] = {
    action.handleErrorWith { error =>
      if (retries > 0) IO.sleep(delay) *> retryIO(retries - 1, delay)(action)
      else IO.raiseError(error)
    }
  }

}
