package core.errors.http.preprocessor

sealed trait ReactionError extends Throwable {
  def message: String
}

object ReactionError {
  case class NotFoundError(message: String) extends ReactionError
  case class CreationError(message: String) extends ReactionError
  case class DeletionError(message: String) extends ReactionError
}
