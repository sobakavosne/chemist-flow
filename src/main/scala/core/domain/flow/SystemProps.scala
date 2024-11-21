package core.domain.flow

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

/**
 * Represents a chemical phase in a flow system.
 *
 * @param name
 *   The name of the phase.
 */
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

/**
 * Represents a property with a value and unit.
 *
 * @param value
 *   The numerical value of the property.
 * @param unit
 *   The unit of the property.
 */
case class Property(value: Double, unit: String)

/**
 * Represents an amount with a name, value, and unit.
 *
 * @param name
 *   The name of the amount (e.g., species or element).
 * @param value
 *   The numerical value of the amount.
 * @param unit
 *   The unit of the amount.
 */
case class Amount(name: String, value: Double, unit: String)

/**
 * Represents activity information for a species or component.
 *
 * @param name
 *   The name of the species or component.
 * @param value
 *   The activity value.
 * @param unit
 *   The unit of the activity.
 */
case class ActivityInfo(name: String, value: Double, unit: String)

/**
 * Represents energy information for a species or system.
 *
 * @param name
 *   The name of the energy component (e.g., Gibbs energy).
 * @param value
 *   The numerical value of the energy.
 * @param unit
 *   The unit of the energy.
 */
case class EnergyInfo(name: String, value: Double, unit: String)

/**
 * Represents the mole fraction of a species or component.
 *
 * @param name
 *   The name of the species or component.
 * @param value
 *   The mole fraction value.
 * @param unit
 *   The unit of the mole fraction (usually unitless or percentage).
 */
case class MoleFraction(name: String, value: Double, unit: String)

/**
 * Represents the heat capacity of a species or system.
 *
 * @param name
 *   The name of the heat capacity component.
 * @param value
 *   The numerical value of the heat capacity.
 * @param unit
 *   The unit of the heat capacity.
 */
case class HeatCapacity(name: String, value: Double, unit: String)

/**
 * Represents the system properties of a chemical flow system.
 *
 * @param generalProperties
 *   A map of general properties with their corresponding values and units.
 * @param elementAmounts
 *   A list of element amounts in the system.
 * @param speciesAmounts
 *   A list of species amounts in the system.
 * @param moleFractions
 *   A list of mole fractions for species or components.
 * @param activityCoefficients
 *   A list of activity coefficients for species or components.
 * @param activities
 *   A list of activities for species or components.
 * @param logActivities
 *   A list of log-scaled activities for species or components.
 * @param lnActivities
 *   A list of natural-log-scaled activities for species or components.
 * @param chemicalPotentials
 *   A list of chemical potential energies for species or components.
 * @param standardVolumes
 *   A list of standard volumes for species or components.
 * @param standardGibbsEnergies
 *   A list of standard Gibbs free energies for species or components.
 * @param standardEnthalpies
 *   A list of standard enthalpies for species or components.
 * @param standardEntropies
 *   A list of standard entropies for species or components.
 * @param standardInternalEnergies
 *   A list of standard internal energies for species or components.
 * @param standardHelmholtzEnergies
 *   A list of standard Helmholtz free energies for species or components.
 * @param standardHeatCapacitiesP
 *   A list of standard heat capacities at constant pressure.
 * @param standardHeatCapacitiesV
 *   A list of standard heat capacities at constant volume.
 */
case class SystemProps(
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

object SystemProps {
  implicit val encoder: Encoder[SystemProps] = deriveEncoder
  implicit val decoder: Decoder[SystemProps] = deriveDecoder
}
