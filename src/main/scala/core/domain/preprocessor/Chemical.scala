package core.domain.preprocessor

import io.circe.{Decoder, Encoder}
import io.circe.{KeyDecoder, KeyEncoder}
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
  implicit val encoder: Encoder[Molecule] = deriveEncoder[Molecule]
  implicit val decoder: Decoder[Molecule] = deriveDecoder[Molecule]

  implicit val keyEncoder: KeyEncoder[Molecule] = KeyEncoder.instance { molecule =>
    s"${molecule.moleculeId}-${molecule.moleculeSmiles}"
  }

  implicit val keyDecoder: KeyDecoder[Molecule] = KeyDecoder.instance { key =>
    key.split("-", 2) match {
      case Array(id, smiles) => Some(Molecule(id.toInt, smiles, "")) // Assuming empty IUPAC name
      case _                 => None
    }
  }

}

object Reaction {
  implicit val encoder: Encoder[Reaction] = deriveEncoder[Reaction]
  implicit val decoder: Decoder[Reaction] = deriveDecoder[Reaction]
}

object Catalyst {
  implicit val encoder: Encoder[Catalyst] = deriveEncoder[Catalyst]
  implicit val decoder: Decoder[Catalyst] = deriveDecoder[Catalyst]
}

object PRODUCT_FROM {
  implicit val encoder: Encoder[PRODUCT_FROM] = deriveEncoder[PRODUCT_FROM]
  implicit val decoder: Decoder[PRODUCT_FROM] = deriveDecoder[PRODUCT_FROM]
}

object REAGENT_IN {
  implicit val encoder: Encoder[REAGENT_IN] = deriveEncoder[REAGENT_IN]
  implicit val decoder: Decoder[REAGENT_IN] = deriveDecoder[REAGENT_IN]
}

object ACCELERATE {
  implicit val encoder: Encoder[ACCELERATE] = deriveEncoder[ACCELERATE]
  implicit val decoder: Decoder[ACCELERATE] = deriveDecoder[ACCELERATE]
}

object InboundReagent {
  implicit val encoder: Encoder[InboundReagent] = deriveEncoder[InboundReagent]
  implicit val decoder: Decoder[InboundReagent] = deriveDecoder[InboundReagent]
}

object OutboundProduct {
  implicit val encoder: Encoder[OutboundProduct] = deriveEncoder[OutboundProduct]
  implicit val decoder: Decoder[OutboundProduct] = deriveDecoder[OutboundProduct]
}

object Condition {
  implicit val encoder: Encoder[Condition] = deriveEncoder[Condition]
  implicit val decoder: Decoder[Condition] = deriveDecoder[Condition]
}
