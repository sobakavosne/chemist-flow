package core.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed trait Interactant

object Interactant {
  implicit val interactantEncoder: Encoder[Interactant] = deriveEncoder
  implicit val interactantDecoder: Decoder[Interactant] = deriveDecoder
}

case class IAccelerate(accelerate: ACCELERATE) extends Interactant
case class ICatalyst(catalyst: Catalyst) extends Interactant
case class IMolecule(molecule: Molecule) extends Interactant
case class IProductFrom(productFrom: PRODUCT_FROM) extends Interactant
case class IReagentIn(reagentIn: REAGENT_IN) extends Interactant
case class IReaction(reaction: Reaction) extends Interactant

object IAccelerate {
  implicit val iAccelerateEncoder: Encoder[IAccelerate] = deriveEncoder
  implicit val iAccelerateDecoder: Decoder[IAccelerate] = deriveDecoder
}

object ICatalyst {
  implicit val iCatalystEncoder: Encoder[ICatalyst] = deriveEncoder
  implicit val iCatalystDecoder: Decoder[ICatalyst] = deriveDecoder
}

object IMolecule {
  implicit val iMoleculeEncoder: Encoder[IMolecule] = deriveEncoder
  implicit val iMoleculeDecoder: Decoder[IMolecule] = deriveDecoder
}

object IProductFrom {
  implicit val iProductFromEncoder: Encoder[IProductFrom] = deriveEncoder
  implicit val iProductFromDecoder: Decoder[IProductFrom] = deriveDecoder
}

object IReagentIn {
  implicit val iReagentInEncoder: Encoder[IReagentIn] = deriveEncoder
  implicit val iReagentInDecoder: Decoder[IReagentIn] = deriveDecoder
}

object IReaction {
  implicit val iReactionEncoder: Encoder[IReaction] = deriveEncoder
  implicit val iReactionDecoder: Decoder[IReaction] = deriveDecoder
}

sealed trait Explain

case class EMechanism(mechanism: Mechanism) extends Explain
case class EStage(stage: Stage) extends Explain

object Explain {
  implicit val eMechanismEncoder: Encoder[EMechanism] = deriveEncoder
  implicit val eMechanismDecoder: Decoder[EMechanism] = deriveDecoder

  implicit val eStageEncoder: Encoder[EStage] = deriveEncoder
  implicit val eStageDecoder: Decoder[EStage] = deriveDecoder
}
