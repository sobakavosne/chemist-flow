package core.repositories

import cats.effect.{Ref, Sync}
import cats.implicits.toFunctorOps
import core.domain.{Reaction, ReactionId}
import types.ReactionRepository
import core.errors.ReactionError
import core.errors.ReactionError.CreationError

class InMemoryReactionRepository[F[_]: Sync](state: Ref[F, Map[ReactionId, Reaction]]) extends ReactionRepository[F] {
  private def generateId(currentState: Map[ReactionId, Reaction]): Int =
    currentState.keys.maxOption.getOrElse(0) + 1

  def get(id: ReactionId): F[Option[Reaction]] =
    state.get.map(_.get(id))

  def create(reaction: Reaction): F[Either[ReactionError, Reaction]] = {
    state.modify { reactions =>
      val id = generateId(reactions)
      if (reactions.values.exists(_.name == reaction.name)) {
        (reactions, Left(CreationError(s"Reaction with name '${reaction.name}' already exists")))
      } else {
        val newReaction = reaction.copy(id)
        (reactions + (id -> newReaction), Right(newReaction))
      }
    }
  }

  def delete(id: ReactionId): F[Boolean] =
    state.modify { reactions =>
      if (reactions.contains(id)) (reactions - id, true)
      else (reactions, false)
    }
}
