package core.errors

sealed trait MechanismError extends Throwable {
  def message: String
}

sealed trait ReactionError extends Throwable {
  def message: String
}

object MechanismError {
  case class CreationError(message: String) extends MechanismError
}

object ReactionError {
  case class CreationError(message: String) extends ReactionError
}
