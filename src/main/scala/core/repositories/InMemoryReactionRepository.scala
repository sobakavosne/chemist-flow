// package core.repositories

// import cats.effect.{Ref, Sync}
// import cats.implicits._
// import core.domain.Reaction

// class InMemoryReactionRepository[F[_]: Sync](state: Ref[F, Map[Int, Reaction]]) extends ReactionRepository[F] {

//   private def generateId(currentState: Map[Int, Reaction]): Int =
//     if (currentState.isEmpty) 1 else currentState.keys.max + 1

//   def get(id: Int): F[Option[Reaction]] =
//     state.get.map(_.get(id))

//   def create(reaction: Reaction): F[Reaction] =
//     state.modify { reactions =>
//       val id          = generateId(reactions)
//       val newReaction = reaction.copy(reactionId = id)
//       (reactions + (id -> newReaction), newReaction)
//     }

//   def update(id: Int, reaction: Reaction): F[Option[Reaction]] =
//     state.modify { reactions =>
//       reactions.get(id) match {
//         case Some(_) =>
//           val updatedReaction = reaction.copy(reactionId = id)
//           (reactions + (id -> updatedReaction), Some(updatedReaction))
//         case None => (reactions, None)
//       }
//     }

//   def delete(id: Int): F[Boolean] =
//     state.modify { reactions =>
//       if (reactions.contains(id)) (reactions - id, true)
//       else (reactions, false)
//     }
// }
