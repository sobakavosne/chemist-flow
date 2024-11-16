package core.domain.flow

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import core.domain.preprocessor.Molecule
import core.domain.preprocessor.Molecule._

case class DataBase(name: String)

object DataBase {
  val ThermoFunDatabaseSlop    = DataBase("slop98")
  val PhreeqcDatabase          = DataBase("phreeqc.dat")
  val ThermoFunDatabaseCemdata = DataBase("cemdata18")

  def custom(name: String): DataBase = DataBase(name)

  implicit val encoder: Encoder[DataBase] = deriveEncoder
  implicit val decoder: Decoder[DataBase] = deriveDecoder
}

case class SystemState(
  temperature:     Double,
  pressure:        Double,
  database:        DataBase,
  moleculeAmounts: Map[Molecule, Double]
)

object SystemState {
  implicit val encoder: Encoder[SystemState] = deriveEncoder
  implicit val decoder: Decoder[SystemState] = deriveDecoder
}
