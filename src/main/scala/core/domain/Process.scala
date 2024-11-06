package resource.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import chemical.{ACCELERATE, Catalyst, Molecule, PRODUCT_FROM, REAGENT_IN, Reaction}

case class ReactionDetails(
  reaction: Reaction,
  inboundReagents: List[(REAGENT_IN, Molecule)],
  outboundProducts: List[(PRODUCT_FROM, Molecule)],
  conditions: List[(ACCELERATE, Catalyst)]
)

object ReactionDetails {
  implicit val reactionDetailsEncoder: Encoder[ReactionDetails] = deriveEncoder[ReactionDetails]
  implicit val reactionDetailsDecoder: Decoder[ReactionDetails] = deriveDecoder[ReactionDetails]
}
