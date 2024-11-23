package core.repositories

import cats.effect.{Ref, Sync}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId, catsSyntaxEq, toFlatMapOps, toFunctorOps}

import core.domain.preprocessor.{Mechanism, MechanismId}
import core.errors.http.preprocessor.MechanismError
import core.errors.http.preprocessor.MechanismError.CreationError

import types.MechanismRepository

/**
 * `InMemoryMechanismRepository` is analogous to a Haskell stateful data structure that holds a `Map` within a monadic
 * context. This class abstracts over an effect type `F`, which can be seen as a Haskell monad that supports side
 * effects and state management.
 *
 * @param state
 *   Ref[F, Map[MechanismId, Mechanism]]
 *   - `Ref` in Scala is similar to `IORef` or `MVar` in Haskell, representing mutable state within a monad.
 *   - `Map[MechanismId, Mechanism]` represents an immutable key-value data structure, comparable to `Data.Map` in
 *     Haskell.
 *   - `F[_]: Sync` constraint in Scala corresponds to a Haskell `MonadIO` constraint, enabling us to manage effects in
 *     a functional way.
 *
 * @tparam F
 *   The abstract effect type, which could be likened to an effectful monad in Haskell (e.g., `IO`, `StateT`).
 *
 * type MechanismRepository m = StateT (Map MechanismId Mechanism) m
 */
class InMemoryMechanismRepository[F[_]: Sync](state: Ref[F, Map[MechanismId, Mechanism]])
    extends MechanismRepository[F] {

  /**
   * Generates a new unique `MechanismId` based on the current state.
   *
   * This function is analogous to a pure function in Haskell:
   *
   * `generateId` :: `Map MechanismId Mechanism` -> `MechanismId`
   *
   * It takes an immutable `Map` and returns a new unique ID by finding the maximum key and adding 1. Using `maxOption`,
   * it safely handles the case of an empty map by defaulting to 0.
   *
   * In Haskell, `Map` is also immutable by default, so this function would work on a snapshot of the state there as
   * well.
   */
  private def generateId(currentState: Map[MechanismId, Mechanism]): MechanismId =
    currentState.keys.maxOption.getOrElse(0) + 1

  /**
   * Retrieves a Mechanism by its identifier.
   *
   * This function’s signature in Haskell might look like:
   *
   * get :: MonadIO m => Mechanism -> MechanismRepository m (Either MechanismError Mechanism)
   *
   *   - The `Option[Mechanism]` is analogous to `Maybe Mechanism` in Haskell.
   *   - The monadic context `F` represents the effect type (like `StateT` or `IO`), enabling access to the mutable
   *     state `Ref`.
   */
  def get(id: MechanismId): F[Option[Mechanism]] =
    state.get.map(_.get(id))

  /**
   * Creates a new Mechanism entry, assigning it a unique identifier, and updates the state.
   *
   * Haskell equivalent signature:
   *
   * create :: MonadIO m => Mechanism -> MechanismRepository (Map MechanismId Mechanism) m Mechanism
   *
   *   - This function modifies the state, analogous to Haskell’s `StateT` monad transformer with `modify`.
   *   - `state.modify` here acts like `modify` in Haskell’s `State` monad, updating the map with the new Mechanism.
   *   - The `copy` method in Scala can be thought of as `record syntax` in Haskell, creating a new `Mechanism` with an
   *     updated `id`.
   */
  def create(mechanism: Mechanism): F[Either[MechanismError, Mechanism]] =
    state.get.flatMap { mechanisms =>
      mechanisms.values
        .find(_.mechanismName === mechanism.mechanismName)
        .fold {
          val newId        = generateId(mechanisms)
          val newMechanism = mechanism.copy(newId)
          state.update(_ + (newId -> newMechanism)).as(newMechanism.asRight[MechanismError])
        } { _ =>
          CreationError(s"Mechanism with name '${mechanism.mechanismName}' already exists")
            .asLeft[Mechanism]
            .pure[F]
        }
    }

  /**
   * Deletes a Mechanism from the state by its identifier.
   *
   * Equivalent Haskell signature:
   *
   * delete :: MonadIO m => MechanismId -> MechanismRepository (Map MechanismId Mechanism) m Bool
   *
   *   - The `modify` function again resembles Haskell’s `StateT modify`, allowing safe state updates within an
   *     effectful context.
   */
  def delete(id: MechanismId): F[Boolean] =
    state.modify { mechanisms =>
      mechanisms.get(id)
        .fold((mechanisms, false))(_ => (mechanisms - id, true))
    }

}
