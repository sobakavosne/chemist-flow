package core.services.cache

import cats.effect.IO
import cats.effect.unsafe.implicits.global
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
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class LocalCacheServiceSpec extends AnyWordSpec with Matchers {
  implicit val ttl: FiniteDuration = 1.seconds

  "LocalCacheService" should {

    "store and retrieve a mechanism using putMechanism and getMechanism" in {
      val cacheService: LocalCacheService[IO] = new LocalCacheService[IO]
      val mechanismId: MechanismId            = 1
      val mechanism: Mechanism                = Mechanism(mechanismId, "mechanism", "type", 1.0)

      val result = for {
        _         <- cacheService.putMechanism(mechanismId, mechanism)
        retrieved <- cacheService.getMechanism(mechanismId)
      } yield retrieved

      result.unsafeRunSync() shouldEqual Some(mechanism)
    }

    "store and retrieve mechanism details using putMechanismDetails and getMechanismDetails" in {
      val cacheService: LocalCacheService[IO] = new LocalCacheService[IO]
      val mechanismId: MechanismId            = 1
      val mechanism: Mechanism                = Mechanism(mechanismId, "mechanism", "type", 1.0)
      val mechanismDetails: MechanismDetails  = MechanismDetails(
        mechanismContext  = (mechanism, FOLLOW("description")),
        stageInteractants = List((Stage(1, "stage", "desc", List()), List()))
      )

      val result = for {
        _         <- cacheService.putMechanismDetails(mechanismId, mechanismDetails)
        retrieved <- cacheService.getMechanismDetails(mechanismId)
      } yield retrieved

      result.unsafeRunSync() shouldEqual Some(mechanismDetails)
    }

    "create a mechanism only if it doesn't exist using createMechanism" in {
      val cacheService: LocalCacheService[IO] = new LocalCacheService[IO]
      val mechanismId: MechanismId            = 2
      val mechanism: Mechanism                = Mechanism(mechanismId, "new-mechanism", "type", 1.0)

      val result = for {
        created <- cacheService.createMechanism(mechanismId, mechanism)
        attempt <- cacheService.createMechanism(mechanismId, mechanism)
      } yield (created, attempt)

      val (created, attempt) = result.unsafeRunSync()

      created shouldEqual Right(mechanism)
      attempt shouldEqual Left(s"Mechanism with ID $mechanismId already exists in cache.")
    }

    "store and retrieve a reaction using putReaction and getReaction" in {
      val cacheService: LocalCacheService[IO] = new LocalCacheService[IO]
      val reactionId: ReactionId              = 1
      val reaction: Reaction                  = Reaction(reactionId, "reaction")

      val result = for {
        _         <- cacheService.putReaction(reactionId, reaction)
        retrieved <- cacheService.getReaction(reactionId)
      } yield retrieved

      result.unsafeRunSync() shouldEqual Some(reaction)
    }

    "store and retrieve reaction details using putReactionDetails and getReactionDetails" in {
      val cacheService: LocalCacheService[IO] = new LocalCacheService[IO]
      val reactionId: ReactionId              = 1
      val reaction: Reaction                  = Reaction(reactionId, "reaction")
      val reactionDetails: ReactionDetails    = ReactionDetails(reaction, List(), List(), List())

      val result = for {
        _         <- cacheService.putReactionDetails(reactionId, reactionDetails)
        retrieved <- cacheService.getReactionDetails(reactionId)
      } yield retrieved

      result.unsafeRunSync() shouldEqual Some(reactionDetails)
    }

    "create a reaction only if it doesn't exist using createReaction" in {
      val cacheService: LocalCacheService[IO] = new LocalCacheService[IO]
      val reactionId: ReactionId              = 2
      val reaction: Reaction                  = Reaction(reactionId, "new-reaction")

      val result = for {
        created <- cacheService.createReaction(reactionId, reaction)
        attempt <- cacheService.createReaction(reactionId, reaction)
      } yield (created, attempt)

      val (created, attempt) = result.unsafeRunSync()

      created shouldEqual Right(reaction)
      attempt shouldEqual Left(s"Reaction with ID $reactionId already exists in cache.")
    }

    "remove expired mechanisms and reactions using cleanExpiredEntries" in {
      val cacheService: LocalCacheService[IO] = new LocalCacheService[IO]
      val mechanismId: MechanismId            = 3
      val mechanism: Mechanism                = Mechanism(mechanismId, "expired-mechanism", "type", 1.0)
      val reactionId: ReactionId              = 3
      val reaction: Reaction                  = Reaction(reactionId, "expired-reaction")

      val result = for {
        _                  <- cacheService.putMechanism(mechanismId, mechanism)
        _                  <- cacheService.putReaction(reactionId, reaction)
        _                  <- IO.sleep(2.seconds)
        _                  <- cacheService.cleanExpiredEntries
        retrievedMechanism <- cacheService.getMechanism(mechanismId)
        retrievedReaction  <- cacheService.getReaction(reactionId)
      } yield (retrievedMechanism, retrievedReaction)

      val (retrievedMechanism, retrievedReaction) = result.unsafeRunSync()

      retrievedMechanism shouldBe None
      retrievedReaction shouldBe None
    }

    "return None for non-existent mechanism IDs" in {
      val cacheService: LocalCacheService[IO] = new LocalCacheService[IO]
      val result: Option[Mechanism]           = cacheService.getMechanism(999).unsafeRunSync()

      result shouldBe None
    }

    "return None for non-existent reaction IDs" in {
      val cacheService: LocalCacheService[IO] = new LocalCacheService[IO]
      val result: Option[Reaction]            = cacheService.getReaction(999).unsafeRunSync()

      result shouldBe None
    }
  }
}
