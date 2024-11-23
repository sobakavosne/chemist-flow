package core.repositories

import cats.effect.IO
import cats.effect.Ref
import cats.effect.unsafe.implicits.global

import core.domain.preprocessor.{Mechanism, MechanismId}
import core.errors.http.preprocessor.MechanismError
import core.errors.http.preprocessor.MechanismError.CreationError

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class InMemoryMechanismRepositorySpec extends AnyWordSpec with Matchers {

  "InMemoryMechanismRepository" should {

    "store and retrieve a mechanism by ID using create and get" in {
      val initialState = Map.empty[MechanismId, Mechanism]
      val ref          = Ref.of[IO, Map[MechanismId, Mechanism]](initialState).unsafeRunSync()
      val repository   = new InMemoryMechanismRepository[IO](ref)

      val mechanism = Mechanism(0, "mechanism-1", "type", 1.0)

      val result = for {
        created   <- repository.create(mechanism)
        retrieved <- repository.get(created.toOption.map(_.mechanismId).get)
      } yield (created, retrieved)

      val (created, retrieved) = result.unsafeRunSync()

      created shouldBe Right(mechanism.copy(1))  // ID auto-generated
      retrieved shouldBe Some(mechanism.copy(1))
    }

    "return an error when creating a mechanism with a duplicate name" in {
      val initialState = Map.empty[MechanismId, Mechanism]
      val ref          = Ref.of[IO, Map[MechanismId, Mechanism]](initialState).unsafeRunSync()
      val repository   = new InMemoryMechanismRepository[IO](ref)

      val mechanism1 = Mechanism(0, "mechanism-1", "type", 1.0)
      val mechanism2 = Mechanism(0, "mechanism-1", "type", 2.0)

      val result = for {
        _       <- repository.create(mechanism1)
        attempt <- repository.create(mechanism2)
      } yield attempt

      val attempt = result.unsafeRunSync()

      attempt shouldBe Left(CreationError("Mechanism with name 'mechanism-1' already exists"))
    }

    "delete a mechanism by ID using delete" in {
      val initialState = Map.empty[MechanismId, Mechanism]
      val ref          = Ref.of[IO, Map[MechanismId, Mechanism]](initialState).unsafeRunSync()
      val repository   = new InMemoryMechanismRepository[IO](ref)

      val mechanism = Mechanism(0, "mechanism-to-delete", "type", 1.0)

      val result = for {
        created   <- repository.create(mechanism)
        deleted   <- repository.delete(created.toOption.map(_.mechanismId).get)
        retrieved <- repository.get(created.toOption.map(_.mechanismId).get)
      } yield (deleted, retrieved)

      val (deleted, retrieved) = result.unsafeRunSync()

      deleted shouldBe true
      retrieved shouldBe None
    }

    "return false when deleting a non-existent mechanism ID" in {
      val initialState = Map.empty[MechanismId, Mechanism]
      val ref          = Ref.of[IO, Map[MechanismId, Mechanism]](initialState).unsafeRunSync()
      val repository   = new InMemoryMechanismRepository[IO](ref)

      val result = repository.delete(999).unsafeRunSync()

      result shouldBe false
    }

    "return None when retrieving a non-existent mechanism ID" in {
      val initialState = Map.empty[MechanismId, Mechanism]
      val ref          = Ref.of[IO, Map[MechanismId, Mechanism]](initialState).unsafeRunSync()
      val repository   = new InMemoryMechanismRepository[IO](ref)

      val result = repository.get(999).unsafeRunSync()

      result shouldBe None
    }
  }
}
