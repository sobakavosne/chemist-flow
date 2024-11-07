package core.repositories.types

import core.domain.{Mechanism, MechanismId}
import core.errors.http.MechanismError

trait MechanismRepository[F[_]] {
  def get(id: MechanismId):         F[Option[Mechanism]]
  def create(mechanism: Mechanism): F[Either[MechanismError, Mechanism]]
  def delete(id: MechanismId):      F[Boolean]
}
