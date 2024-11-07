package core.services

import cats.effect.Sync
import core.domain.{Reaction, ReactionId}
import core.repositories.types.ReactionRepository
import core.errors.ReactionError

class ReactionService[F[_]: Sync](repository: ReactionRepository[F]) {

  def getReaction(id: ReactionId): F[Option[Reaction]] =
    repository.get(id)

  def createReaction(reaction: Reaction): F[Either[ReactionError, Reaction]] =
    repository.create(reaction)

  def deleteReaction(id: ReactionId): F[Boolean] =
    repository.delete(id)
}
