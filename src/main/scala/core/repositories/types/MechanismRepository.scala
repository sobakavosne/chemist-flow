package core.repositories.types

import core.domain.preprocessor.{Mechanism, MechanismId}
import core.errors.http.preprocessor.MechanismError

trait MechanismRepository[F[_]] {
  def get(id: MechanismId):         F[Option[Mechanism]]
  def create(mechanism: Mechanism): F[Either[MechanismError, Mechanism]]
  def delete(id: MechanismId):      F[Boolean]
}
