import akka.actor.ActorSystem
import akka.cluster.{Cluster, MemberStatus}
import akka.cluster.ddata.{DistributedData, SelfUniqueAddress}
import akka.testkit.TestKit

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import com.typesafe.config.Config

import config.ConfigLoader.DefaultConfigLoader

import core.domain.preprocessor.{Mechanism, MechanismId, Reaction, ReactionId}
import core.services.cache.DistributedCacheService

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import core.domain.preprocessor.MechanismDetails
import core.domain.preprocessor.FOLLOW
import core.domain.preprocessor.Stage
import core.domain.preprocessor.ReactionDetails

class DistributedCacheServiceSpec extends AnyFunSuite with Matchers with BeforeAndAfterAll {

  val config: Config                            = DefaultConfigLoader.pureConfig
  val system: ActorSystem                       = ActorSystem("ChemistFlowActorSystem", config)
  val selfUniqueAddress: SelfUniqueAddress      = DistributedData(system).selfUniqueAddress
  val cacheService: DistributedCacheService[IO] = new DistributedCacheService[IO](system, selfUniqueAddress)
  val cluster: Cluster                          = Cluster(system)

  cluster.registerOnMemberUp {
    println("Cluster is fully operational")
  }

  def ensureClusterIsReady(): Unit = {
    val timeout = System.currentTimeMillis() + 5000
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

  test("putMechanism and getMechanism should store and retrieve a mechanism") {
    val mechanismId: MechanismId = 1
    val mechanism: Mechanism     = Mechanism(mechanismId, "mechanism", "type", 1.0)

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

  test("putMechanismDetails and getMechanismDetails should store and retrieve a mechanism details") {
    val mechanismId: MechanismId           = 1
    val mechanism: Mechanism               = Mechanism(mechanismId, "mechanism", "type", 1.0)
    val mechanismDetails: MechanismDetails = MechanismDetails(
      mechanismContext  = (mechanism, FOLLOW("description")),
      stageInteractants = List((Stage(10, "name", "description", List()), List()))
    )

    val result = for {
      _         <- cacheService.putMechanismDetails(mechanismId, mechanismDetails)
      retrieved <- retryIO(10, 500.millis) {
                     cacheService.getMechanismDetails(mechanismId).flatMap {
                       case Some(value) => IO.pure(value)
                       case None        => IO.raiseError(new Exception("Mechanism not yet replicated"))
                     }
                   }
    } yield retrieved

    val retrievedMechanismDetails = result.unsafeRunSync()

    retrievedMechanismDetails shouldEqual mechanismDetails
  }

  test("putReaction and getReaction should store and retrieve a reaction") {
    val reactionId: ReactionId = 1
    val reaction: Reaction     = Reaction(reactionId, "reaction")

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

  test("putReactionDetails and getReactionDetails should store and retrieve a reaction") {
    val reactionId: ReactionId           = 1
    val reaction: Reaction               = Reaction(reactionId, "reaction")
    val reactionDetails: ReactionDetails = ReactionDetails(reaction, List(), List(), List())

    val result = for {
      _         <- cacheService.putReactionDetails(reactionId, reactionDetails)
      retrieved <- retryIO(10, 500.millis) {
                     cacheService.getReactionDetails(reactionId).flatMap {
                       case Some(value) => IO.pure(value)
                       case None        => IO.raiseError(new Exception("Reaction not yet replicated"))
                     }
                   }
    } yield retrieved

    val retrievedReactionDetails = result.unsafeRunSync()

    retrievedReactionDetails shouldEqual reactionDetails
  }

  private def retryIO[A](retries: Int, delay: FiniteDuration)(action: IO[A]): IO[A] = {
    action.handleErrorWith { error =>
      if (retries > 0) IO.sleep(delay) *> retryIO(retries - 1, delay)(action)
      else IO.raiseError(error)
    }
  }

}
