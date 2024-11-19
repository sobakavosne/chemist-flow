package core.errors.http.preprocessor

/**
 * Represents errors related to mechanisms in the HTTP preprocessor.
 */
sealed trait MechanismError extends Throwable {
  def message: String
  override def getMessage: String = message
}

object MechanismError {
  case class NotFoundError(message: String) extends MechanismError
  case class CreationError(message: String) extends MechanismError
  case class DeletionError(message: String) extends MechanismError
  case class NetworkError(message: String) extends MechanismError
  case class HttpError(message: String) extends MechanismError
  case class DecodingError(message: String) extends MechanismError
}
