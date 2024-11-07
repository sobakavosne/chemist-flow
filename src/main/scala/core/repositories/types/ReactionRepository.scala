package core.repositories.types

import core.domain.{Reaction, ReactionId}
import core.errors.http.ReactionError

trait ReactionRepository[F[_]] {
  def get(id: ReactionId):        F[Option[Reaction]]
  def create(reaction: Reaction): F[Either[ReactionError, Reaction]]
  def delete(id: ReactionId):     F[Boolean]
}
