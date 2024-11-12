package core.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

type MoleculeId = Int
type ReactionId = Int
type CatalystId = Int

case class Molecule(
  moleculeId:        MoleculeId,
  moleculeSmiles:    String,
  moleculeIupacName: String
)

case class Reaction(
  reactionId:   ReactionId,
  reactionName: String
)

case class Catalyst(
  catalystId:     CatalystId,
  catalystSmiles: String,
  catalystName:   Option[String]
)

case class PRODUCT_FROM(productAmount: Float)

case class REAGENT_IN(reagentAmount: Float)

case class ACCELERATE(
  temperature: List[Float],
  pressure:    List[Float]
)

case class InboundReagent(
  reagentIn: REAGENT_IN,
  molecule:  Molecule
)

case class OutboundProduct(
  productFrom: PRODUCT_FROM,
  molecule:    Molecule
)

case class Condition(
  accelerate: ACCELERATE,
  catalyst:   Catalyst
)

object Molecule {
  implicit val moleculeEncoder: Encoder[Molecule] = deriveEncoder[Molecule]
  implicit val moleculeDecoder: Decoder[Molecule] = deriveDecoder[Molecule]
}

object Reaction {
  implicit val reactionEncoder: Encoder[Reaction] = deriveEncoder[Reaction]
  implicit val reactionDecoder: Decoder[Reaction] = deriveDecoder[Reaction]
}

object Catalyst {
  implicit val catalystEncoder: Encoder[Catalyst] = deriveEncoder[Catalyst]
  implicit val catalystDecoder: Decoder[Catalyst] = deriveDecoder[Catalyst]
}

object PRODUCT_FROM {
  implicit val productFromEncoder: Encoder[PRODUCT_FROM] = deriveEncoder[PRODUCT_FROM]
  implicit val productFromDecoder: Decoder[PRODUCT_FROM] = deriveDecoder[PRODUCT_FROM]
}

object REAGENT_IN {
  implicit val reagentInEncoder: Encoder[REAGENT_IN] = deriveEncoder[REAGENT_IN]
  implicit val reagentInDecoder: Decoder[REAGENT_IN] = deriveDecoder[REAGENT_IN]
}

object ACCELERATE {
  implicit val accelerateEncoder: Encoder[ACCELERATE] = deriveEncoder[ACCELERATE]
  implicit val accelerateDecoder: Decoder[ACCELERATE] = deriveDecoder[ACCELERATE]
}

object InboundReagent {
  implicit val inboundReagentEncoder: Encoder[InboundReagent] = deriveEncoder[InboundReagent]
  implicit val inboundReagentDecoder: Decoder[InboundReagent] = deriveDecoder[InboundReagent]
}

object OutboundProduct {
  implicit val outboundProductEncoder: Encoder[OutboundProduct] = deriveEncoder[OutboundProduct]
  implicit val outboundProductDecoder: Decoder[OutboundProduct] = deriveDecoder[OutboundProduct]
}

object Condition {
  implicit val conditionEncoder: Encoder[Condition] = deriveEncoder[Condition]
  implicit val conditionDecoder: Decoder[Condition] = deriveDecoder[Condition]
}
