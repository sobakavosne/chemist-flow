package core.repositories

import cats.effect.{Ref, Sync}
import cats.syntax.all._

import core.domain.preprocessor.{Reaction, ReactionId}
import core.errors.http.preprocessor.ReactionError
import core.errors.http.preprocessor.ReactionError.CreationError

import types.ReactionRepository

/**
 * An in-memory implementation of `ReactionRepository` for testing and local use.
 *
 * @param state
 *   A `Ref` encapsulating the mutable map of reactions.
 * @tparam F
 *   Effect type, such as `IO` or `SyncIO`.
 */
class FunctionalInMemoryReactionRepository[F[_]: Sync](state: Ref[F, Map[ReactionId, Reaction]])
    extends ReactionRepository[F] {

  /**
   * Generates a new unique ID for a reaction.
   *
   * @param reactions
   *   The current state of the stored reactions.
   * @return
   *   The next available integer ID.
   */
  private def generateId(reactions: Map[ReactionId, Reaction]): ReactionId =
    reactions.keys.maxOption.fold(1)(_ + 1)

  /**
   * Retrieves a reaction by its ID.
   *
   * @param id
   *   The ID of the reaction to retrieve.
   * @return
   *   Effectful optional `Reaction`. Returns `None` if not found.
   */
  def get(id: ReactionId): F[Option[Reaction]] =
    state.get.map(_.get(id))

  /**
   * Creates a new reaction.
   *
   * @param reaction
   *   The `Reaction` instance to add.
   * @return
   *   Effectful result of `Either` with an error or the new reaction.
   */
  def create(reaction: Reaction): F[Either[ReactionError, Reaction]] =
    state.get.flatMap { reactions =>
      reactions.values
        .find(_.reactionName === reaction.reactionName)
        .fold {
          val newId       = generateId(reactions)
          val newReaction = reaction.copy(newId)
          state.update(_ + (newId -> newReaction)).as(newReaction.asRight[ReactionError])
        } { _ =>
          CreationError(s"Reaction with name '${reaction.reactionName}' already exists")
            .asLeft[Reaction]
            .pure[F]
        }
    }

  /**
   * Deletes a reaction by its ID.
   *
   * @param id
   *   The ID of the reaction to delete.
   * @return
   *   Effectful `Boolean` indicating if the deletion was successful.
   */
  def delete(id: ReactionId): F[Boolean] =
    state.modify(reactions =>
      reactions
        .get(id)
        .fold((reactions, false))(_ => (reactions - id, true))
    )

}
