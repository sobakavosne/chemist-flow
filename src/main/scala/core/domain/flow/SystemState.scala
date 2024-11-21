package core.domain.flow

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import core.domain.preprocessor.Molecule
import core.domain.preprocessor.Molecule._

/**
 * Represents a database used for chemical thermodynamic calculations.
 *
 * @param name
 *   The name of the database.
 */
case class DataBase(name: String)

object DataBase {
  val ThermoFunDatabaseSlop    = DataBase("slop98")
  val PhreeqcDatabase          = DataBase("phreeqc.dat")
  val ThermoFunDatabaseCemdata = DataBase("cemdata18")

  def custom(name: String): DataBase = DataBase(name)

  implicit val encoder: Encoder[DataBase] = deriveEncoder
  implicit val decoder: Decoder[DataBase] = deriveDecoder
}

/**
 * Represents the state of a chemical system.
 *
 * @param temperature
 *   The system's temperature in Kelvin.
 * @param pressure
 *   The system's pressure in Pascal.
 * @param database
 *   The thermodynamic database used for the system.
 * @param moleculeAmounts
 *   A map of molecules to their respective amounts in the system.
 */
case class SystemState(
  temperature:     Double,
  pressure:        Double,
  database:        DataBase,
  moleculeAmounts: Map[Molecule, Double]
)

/**
 * Represents a list of molecule amounts for reagents and products in a reaction.
 *
 * @param inboundReagentAmounts
 *   A list of amounts for inbound reagents.
 * @param outboundProductAmounts
 *   A list of amounts for outbound products.
 */
case class MoleculeAmountList(
  inboundReagentAmounts:  List[Double],
  outboundProductAmounts: List[Double]
)

object SystemState {
  implicit val encoder: Encoder[SystemState] = deriveEncoder
  implicit val decoder: Decoder[SystemState] = deriveDecoder
}

object MoleculeAmountList {
  implicit val encoder: Encoder[MoleculeAmountList] = deriveEncoder
  implicit val decoder: Decoder[MoleculeAmountList] = deriveDecoder
}
