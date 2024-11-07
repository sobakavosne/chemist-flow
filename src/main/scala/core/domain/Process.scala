package core.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class ReactionDetails(
  reaction:         Reaction,
  inboundReagents:  List[(REAGENT_IN, Molecule)],
  outboundProducts: List[(PRODUCT_FROM, Molecule)],
  conditions:       List[(ACCELERATE, Catalyst)]
)

object ReactionDetails {
  implicit val reactionDetailsEncoder: Encoder[ReactionDetails] = deriveEncoder[ReactionDetails]
  implicit val reactionDetailsDecoder: Decoder[ReactionDetails] = deriveDecoder[ReactionDetails]
}

case class MechanismDetails(
  mechanismContext: (Mechanism, FOLLOW),
  stageInteractants: List[(Stage, List[Interactant])]
)

object MechanismDetails {
  implicit val mechanismDetailsEncoder: Encoder[MechanismDetails] = deriveEncoder[MechanismDetails]
  implicit val mechanismDetailsDecoder: Decoder[MechanismDetails] = deriveDecoder[MechanismDetails]
}

case class ProcessDetails(
  reactionDetails:  ReactionDetails,
  mechanismDetails: MechanismDetails
)

object ProcessDetails {
  implicit val processDetailsEncoder: Encoder[ProcessDetails] = deriveEncoder[ProcessDetails]
  implicit val processDetailsDecoder: Decoder[ProcessDetails] = deriveDecoder[ProcessDetails]
}
