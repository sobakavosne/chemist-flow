package core.domain.flow

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Phase(name: String)

object Phase {
  val AqueousPhase     = Phase("AqueousPhase")
  val GaseousPhase     = Phase("GaseousPhase")
  val LiquidPhase      = Phase("LiquidPhase")
  val SolidPhase       = Phase("SolidPhase")
  val MineralPhase     = Phase("MineralPhase")
  val CondensedPhase   = Phase("CondensedPhase")
  val IonExchangePhase = Phase("IonExchangePhase")
}

case class Property(value: Double, unit: String)
case class Amount(name: String, value: Double, unit: String)
case class ActivityInfo(name: String, value: Double, unit: String)
case class EnergyInfo(name: String, value: Double, unit: String)
case class MoleFraction(name: String, value: Double, unit: String)
case class HeatCapacity(name: String, value: Double, unit: String)

case class ChemicalProps(
  generalProperties:         Map[String, Property],
  elementAmounts:            List[Amount],
  speciesAmounts:            List[Amount],
  moleFractions:             List[MoleFraction],
  activityCoefficients:      List[ActivityInfo],
  activities:                List[ActivityInfo],
  logActivities:             List[ActivityInfo],
  lnActivities:              List[ActivityInfo],
  chemicalPotentials:        List[EnergyInfo],
  standardVolumes:           List[Amount],
  standardGibbsEnergies:     List[EnergyInfo],
  standardEnthalpies:        List[EnergyInfo],
  standardEntropies:         List[EnergyInfo],
  standardInternalEnergies:  List[EnergyInfo],
  standardHelmholtzEnergies: List[EnergyInfo],
  standardHeatCapacitiesP:   List[HeatCapacity],
  standardHeatCapacitiesV:   List[HeatCapacity]
)

object Property {
  implicit val encoder: Encoder[Property] = deriveEncoder
  implicit val decoder: Decoder[Property] = deriveDecoder
}

object Amount {
  implicit val encoder: Encoder[Amount] = deriveEncoder
  implicit val decoder: Decoder[Amount] = deriveDecoder
}

object ActivityInfo {
  implicit val encoder: Encoder[ActivityInfo] = deriveEncoder
  implicit val decoder: Decoder[ActivityInfo] = deriveDecoder
}

object EnergyInfo {
  implicit val encoder: Encoder[EnergyInfo] = deriveEncoder
  implicit val decoder: Decoder[EnergyInfo] = deriveDecoder
}

object MoleFraction {
  implicit val encoder: Encoder[MoleFraction] = deriveEncoder
  implicit val decoder: Decoder[MoleFraction] = deriveDecoder
}

object HeatCapacity {
  implicit val encoder: Encoder[HeatCapacity] = deriveEncoder
  implicit val decoder: Decoder[HeatCapacity] = deriveDecoder
}

object ThermodynamicData {
  implicit val encoder: Encoder[ChemicalProps] = deriveEncoder
  implicit val decoder: Decoder[ChemicalProps] = deriveDecoder
}
