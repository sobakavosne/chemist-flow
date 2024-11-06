package core.repositories

import core.domain.Mechanism

trait MechanismRepository[F[_]] {
  def get(id: Int): F[Option[Mechanism]]
  def create(mechanism: Mechanism): F[Mechanism]
  def update(id: Int, mechanism: Mechanism): F[Option[Mechanism]]
  def delete(id: Int): F[Boolean]
}
