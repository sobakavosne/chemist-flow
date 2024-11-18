package core.repositories.types

import core.domain.preprocessor.{Reaction, ReactionId}
import core.errors.http.preprocessor.ReactionError

trait ReactionRepository[F[_]] {
  def get(id: ReactionId):        F[Option[Reaction]]
  def create(reaction: Reaction): F[Either[ReactionError, Reaction]]
  def delete(id: ReactionId):     F[Boolean]
}
