package core.errors.http.preprocessor

sealed trait ReactionError extends Throwable {
  def message: String
  override def getMessage: String = message
}

object ReactionError {
  case class NotFoundError(message: String) extends ReactionError
  case class CreationError(message: String) extends ReactionError
  case class DeletionError(message: String) extends ReactionError
  case class NetworkError(message: String) extends ReactionError
  case class HttpError(message: String) extends ReactionError
  case class DecodingError(message: String) extends ReactionError
}
