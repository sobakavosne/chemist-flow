package core.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import cats.implicits.toFunctorOps

sealed trait Interactant

case class IMolecule(molecule: Molecule) extends Interactant
case class ICatalyst(catalyst: Catalyst) extends Interactant

object Interactant {

  implicit val interactantEncoder: Encoder[Interactant] = Encoder.instance {
    case i: IMolecule     => IMolecule.iMoleculeEncoder(i)
    case c: ICatalyst     => ICatalyst.iCatalystEncoder(c)
    case a: IAccelerate   => IAccelerate.iAccelerateEncoder(a)
    case pf: IProductFrom => IProductFrom.iProductFromEncoder(pf)
    case ri: IReagentIn   => IReagentIn.iReagentInEncoder(ri)
    case r: IReaction     => IReaction.iReactionEncoder(r)
  }

  implicit val interactantDecoder: Decoder[Interactant] = List[Decoder[Interactant]](
    Decoder[IMolecule].widen,
    Decoder[ICatalyst].widen,
    Decoder[IAccelerate].widen,
    Decoder[IProductFrom].widen,
    Decoder[IReagentIn].widen,
    Decoder[IReaction].widen
  ).reduceLeft(_ or _)

}

object IMolecule {
  implicit val iMoleculeEncoder: Encoder[IMolecule] = deriveEncoder[IMolecule]
  implicit val iMoleculeDecoder: Decoder[IMolecule] = deriveDecoder[IMolecule]
}

object ICatalyst {
  implicit val iCatalystEncoder: Encoder[ICatalyst] = deriveEncoder[ICatalyst]
  implicit val iCatalystDecoder: Decoder[ICatalyst] = deriveDecoder[ICatalyst]
}

case class IAccelerate(accelerate: ACCELERATE) extends Interactant
case class IProductFrom(productFrom: PRODUCT_FROM) extends Interactant
case class IReagentIn(reagentIn: REAGENT_IN) extends Interactant
case class IReaction(reaction: Reaction) extends Interactant

object IAccelerate {
  implicit val iAccelerateEncoder: Encoder[IAccelerate] = deriveEncoder
  implicit val iAccelerateDecoder: Decoder[IAccelerate] = deriveDecoder
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
