package resource.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import chemical.{Accelerate, Catalyst, Molecule, ProductFrom, Reaction, ReagentIn}

case class ReactionDetails(
  reaction: Reaction,
  inboundReagents: List[(ReagentIn, Molecule)],
  outboundProducts: List[(ProductFrom, Molecule)],
  conditions: List[(Accelerate, Catalyst)]
)

object ReactionDetails {
  implicit val reactionDetailsEncoder: Encoder[ReactionDetails] = deriveEncoder[ReactionDetails]
  implicit val reactionDetailsDecoder: Decoder[ReactionDetails] = deriveDecoder[ReactionDetails]
}
