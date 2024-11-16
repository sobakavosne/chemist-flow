package core.errors.http.flow

sealed trait ChemicalPropsError extends Throwable {
  def message: String
}

object ChemicalPropsError {
  case class BadRequestError(message: String) extends ChemicalPropsError
  case class ChemistEngineError(message: String) extends ChemicalPropsError
}
