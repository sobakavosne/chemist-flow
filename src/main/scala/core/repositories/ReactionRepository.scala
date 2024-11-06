package core.repositories

import core.domain.Reaction

trait ReactionRepository[F[_]] {
  def get(id: Int): F[Option[Reaction]]
  def create(reaction: Reaction): F[Reaction]
  def update(id: Int, reaction: Reaction): F[Option[Reaction]]
  def delete(id: Int): F[Boolean]
}
