package core.repositories

import cats.effect.{Ref, Sync}
import cats.implicits.toFunctorOps
import core.domain.preprocessor.{Reaction, ReactionId}
import types.ReactionRepository
import core.errors.http.preprocessor.ReactionError
import core.errors.http.preprocessor.ReactionError.CreationError

/**
 * An in-memory implementation of the `ReactionRepository` for testing and local use.
 *
 * @param state
 *   A reference to a mutable map representing the current state of stored reactions.
 * @tparam F
 *   The effect type (e.g., `IO`, `SyncIO`, etc.).
 */
class InMemoryReactionRepository[F[_]: Sync](state: Ref[F, Map[ReactionId, Reaction]])
    extends ReactionRepository[F] {

  /**
   * Generates a new unique ID for a reaction.
   *
   * @param currentState
   *   The current state of the stored reactions.
   * @return
   *   The next available integer ID.
   */
  private def generateId(currentState: Map[ReactionId, Reaction]): Int =
    currentState.keys.maxOption.getOrElse(0) + 1

  /**
   * Retrieves a reaction by its ID.
   *
   * @param id
   *   The ID of the reaction to retrieve.
   * @return
   *   An effect wrapping an optional `Reaction`. Returns `None` if the reaction is not found.
   */
  def get(id: ReactionId): F[Option[Reaction]] =
    state.get.map(_.get(id))

  /**
   * Creates a new reaction and stores it in the repository.
   *
   * @param reaction
   *   The `Reaction` instance to create.
   * @return
   *   An effect wrapping either a `ReactionError` if the creation fails or the created `Reaction` on success.
   *   - If a reaction with the same name already exists, returns a `CreationError`.
   */
  def create(reaction: Reaction): F[Either[ReactionError, Reaction]] = {
    state.modify { reactions =>
      val id = generateId(reactions)
      if (reactions.values.exists(_.reactionName == reaction.reactionName)) {
        (reactions, Left(CreationError(s"Reaction with name '${reaction.reactionName}' already exists")))
      } else {
        val newReaction = reaction.copy(id)
        (reactions + (id -> newReaction), Right(newReaction))
      }
    }
  }

  /**
   * Deletes a reaction by its ID.
   *
   * @param id
   *   The ID of the reaction to delete.
   * @return
   *   An effect wrapping a `Boolean` indicating whether the deletion was successful.
   *   - Returns `true` if the reaction was successfully deleted.
   *   - Returns `false` if the reaction ID was not found.
   */
  def delete(id: ReactionId): F[Boolean] =
    state.modify { reactions =>
      if (reactions.contains(id)) (reactions - id, true)
      else (reactions, false)
    }

}
