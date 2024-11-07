package core.errors.http

sealed trait MechanismError extends Throwable {
  def message: String
}

object MechanismError {
  case class NotFoundError(message: String) extends MechanismError
  case class CreationError(message: String) extends MechanismError
  case class DeletionError(message: String) extends MechanismError
}
