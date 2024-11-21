package core.domain.preprocessor

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

/**
 * Represents the details of a reaction, including its reagents, products, and conditions.
 *
 * @param reaction
 *   The `Reaction` instance representing the reaction.
 * @param inboundReagents
 *   A list of tuples containing the reagent information (`REAGENT_IN`) and the associated molecule (`Molecule`).
 * @param outboundProducts
 *   A list of tuples containing the product information (`PRODUCT_FROM`) and the associated molecule (`Molecule`).
 * @param conditions
 *   A list of tuples containing acceleration conditions (`ACCELERATE`) and the associated catalyst (`Catalyst`).
 */
case class ReactionDetails(
  reaction:         Reaction,
  inboundReagents:  List[(REAGENT_IN, Molecule)],
  outboundProducts: List[(PRODUCT_FROM, Molecule)],
  conditions:       List[(ACCELERATE, Catalyst)]
)

/**
 * Represents the details of a mechanism, including its context and stage interactants.
 *
 * @param mechanismContext
 *   A tuple containing the `Mechanism` and its follow relationship (`FOLLOW`).
 * @param stageInteractants
 *   A list of tuples containing the `Stage` and the associated list of `Interactant` instances.
 */
case class MechanismDetails(
  mechanismContext: (Mechanism, FOLLOW),
  stageInteractants: List[(Stage, List[Interactant])]
)

/**
 * Represents the combined details of a reaction and its associated mechanism.
 *
 * @param reactionDetails
 *   The `ReactionDetails` instance containing details of the reaction.
 * @param mechanismDetails
 *   The `MechanismDetails` instance containing details of the mechanism.
 */
case class ProcessDetails(
  reactionDetails:  ReactionDetails,
  mechanismDetails: MechanismDetails
)

object ReactionDetails {
  implicit val encoder: Encoder[ReactionDetails] = deriveEncoder[ReactionDetails]
  implicit val decoder: Decoder[ReactionDetails] = deriveDecoder[ReactionDetails]
}

object MechanismDetails {
  implicit val encoder: Encoder[MechanismDetails] = deriveEncoder[MechanismDetails]
  implicit val decoder: Decoder[MechanismDetails] = deriveDecoder[MechanismDetails]
}

object ProcessDetails {
  implicit val encoder: Encoder[ProcessDetails] = deriveEncoder[ProcessDetails]
  implicit val decoder: Decoder[ProcessDetails] = deriveDecoder[ProcessDetails]
}
