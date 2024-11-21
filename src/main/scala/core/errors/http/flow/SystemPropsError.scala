package core.errors.http.flow

/**
 * Represents errors related to system properties in the HTTP flow.
 */
sealed trait SystemPropsError extends Throwable {
  def message: String
}

object SystemPropsError {
  case class BadRequestError(message: String) extends SystemPropsError
  case class ChemistEngineError(message: String) extends SystemPropsError
}
