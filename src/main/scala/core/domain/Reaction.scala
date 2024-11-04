package core.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Reaction(
  id: String,
  name: String,
  description: String,
  reactants: List[Interactant],
  products: List[Interactant]
)

object Reaction {
  implicit val reactionEncoder: Encoder[Reaction] = deriveEncoder[Reaction]
  implicit val reactionDecoder: Decoder[Reaction] = deriveDecoder[Reaction]
}
