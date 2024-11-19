package core.domain.preprocessor

import io.circe.{Decoder, Encoder}
import io.circe.{KeyDecoder, KeyEncoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

type MoleculeId = Int
type ReactionId = Int
type CatalystId = Int

/**
 * Represents a molecule with its ID, SMILES string, and IUPAC name.
 *
 * @param moleculeId
 *   The unique identifier for the molecule.
 * @param moleculeSmiles
 *   The SMILES (Simplified Molecular Input Line Entry System) representation of the molecule.
 * @param moleculeIupacName
 *   The IUPAC (International Union of Pure and Applied Chemistry) name of the molecule.
 */
case class Molecule(
  moleculeId:        MoleculeId,
  moleculeSmiles:    String,
  moleculeIupacName: String
)

/**
 * Represents a chemical reaction with its ID and name.
 *
 * @param reactionId
 *   The unique identifier for the reaction.
 * @param reactionName
 *   The name of the reaction.
 */
case class Reaction(
  reactionId:   ReactionId,
  reactionName: String
)

/**
 * Represents a catalyst used in a chemical reaction.
 *
 * @param catalystId
 *   The unique identifier for the catalyst.
 * @param catalystSmiles
 *   The SMILES representation of the catalyst.
 * @param catalystName
 *   The optional name of the catalyst.
 */
case class Catalyst(
  catalystId:     CatalystId,
  catalystSmiles: String,
  catalystName:   Option[String]
)

/**
 * Represents the amount of product generated from a reaction.
 *
 * @param productAmount
 *   The amount of product formed, in a floating-point representation.
 */
case class PRODUCT_FROM(productAmount: Float)

/**
 * Represents the amount of reagent involved in a reaction.
 *
 * @param reagentAmount
 *   The amount of reagent used, in a floating-point representation.
 */
case class REAGENT_IN(reagentAmount: Float)

/**
 * Represents acceleration conditions for a reaction, such as temperature and pressure ranges.
 *
 * @param temperature
 *   A list of temperatures, in floating-point representation.
 * @param pressure
 *   A list of pressures, in floating-point representation.
 */
case class ACCELERATE(
  temperature: List[Float],
  pressure:    List[Float]
)

/**
 * Represents an inbound reagent in a reaction.
 *
 * @param reagentIn
 *   The reagent's amount information.
 * @param molecule
 *   The molecule associated with the reagent.
 */
case class InboundReagent(
  reagentIn: REAGENT_IN,
  molecule:  Molecule
)

/**
 * Represents an outbound product in a reaction.
 *
 * @param productFrom
 *   The product's amount information.
 * @param molecule
 *   The molecule associated with the product.
 */
case class OutboundProduct(
  productFrom: PRODUCT_FROM,
  molecule:    Molecule
)

/**
 * Represents the conditions for a chemical reaction.
 *
 * @param accelerate
 *   The acceleration conditions (temperature and pressure).
 * @param catalyst
 *   The catalyst used in the reaction.
 */
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
