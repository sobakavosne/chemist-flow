package core.domain.flow

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import core.domain.preprocessor.Molecule
import core.domain.preprocessor.Molecule._

case class DataBase(name: String)

object DataBase {
  val thermoFunDatabaseSlop    = DataBase("slop98")
  val phreeqcDatabase          = DataBase("phreeqc.dat")
  val thermoFunDatabaseCemdata = DataBase("cemdata18")

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

case class MoleculeAmountList(
  inboundReagentAmounts:  List[Double],
  outboundProductAmounts: List[Double]
)

object MoleculeAmountList {
  implicit val encoder: Encoder[MoleculeAmountList] = deriveEncoder
  implicit val decoder: Decoder[MoleculeAmountList] = deriveDecoder
}
