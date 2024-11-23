package core.repositories

import cats.effect.IO
import cats.effect.Ref
import cats.effect.unsafe.implicits.global

import core.domain.preprocessor.{Reaction, ReactionId}
import core.errors.http.preprocessor.ReactionError.CreationError

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class InMemoryReactionRepositorySpec extends AnyWordSpec with Matchers {

  "InMemoryReactionRepository" should {

    "store and retrieve a reaction by ID using create and get" in {
      val initialState = Map.empty[ReactionId, Reaction]
      val ref          = Ref.of[IO, Map[ReactionId, Reaction]](initialState).unsafeRunSync()
      val repository   = new FunctionalInMemoryReactionRepository[IO](ref)

      val reaction = Reaction(0, "reaction-1")

      val result = for {
        created   <- repository.create(reaction)
        retrieved <- repository.get(created.toOption.map(_.reactionId).get)
      } yield (created, retrieved)

      val (created, retrieved) = result.unsafeRunSync()

      created shouldBe Right(reaction.copy(1))  // ID auto-generated
      retrieved shouldBe Some(reaction.copy(1))
    }

    "return an error when creating a reaction with a duplicate name" in {
      val initialState = Map.empty[ReactionId, Reaction]
      val ref          = Ref.of[IO, Map[ReactionId, Reaction]](initialState).unsafeRunSync()
      val repository   = new FunctionalInMemoryReactionRepository[IO](ref)

      val reaction1 = Reaction(0, "reaction-1")
      val reaction2 = Reaction(0, "reaction-1")

      val result = for {
        _       <- repository.create(reaction1)
        attempt <- repository.create(reaction2)
      } yield attempt

      val attempt = result.unsafeRunSync()

      attempt shouldBe Left(CreationError("Reaction with name 'reaction-1' already exists"))
    }

    "delete a reaction by ID using delete" in {
      val initialState = Map.empty[ReactionId, Reaction]
      val ref          = Ref.of[IO, Map[ReactionId, Reaction]](initialState).unsafeRunSync()
      val repository   = new FunctionalInMemoryReactionRepository[IO](ref)

      val reaction = Reaction(0, "reaction-to-delete")

      val result = for {
        created   <- repository.create(reaction)
        deleted   <- repository.delete(created.toOption.map(_.reactionId).get)
        retrieved <- repository.get(created.toOption.map(_.reactionId).get)
      } yield (deleted, retrieved)

      val (deleted, retrieved) = result.unsafeRunSync()

      deleted shouldBe true
      retrieved shouldBe None
    }

    "return false when deleting a non-existent reaction ID" in {
      val initialState = Map.empty[ReactionId, Reaction]
      val ref          = Ref.of[IO, Map[ReactionId, Reaction]](initialState).unsafeRunSync()
      val repository   = new FunctionalInMemoryReactionRepository[IO](ref)

      val result = repository.delete(999).unsafeRunSync()

      result shouldBe false
    }

    "return None when retrieving a non-existent reaction ID" in {
      val initialState = Map.empty[ReactionId, Reaction]
      val ref          = Ref.of[IO, Map[ReactionId, Reaction]](initialState).unsafeRunSync()
      val repository   = new FunctionalInMemoryReactionRepository[IO](ref)

      val result = repository.get(999).unsafeRunSync()

      result shouldBe None
    }
  }
}
