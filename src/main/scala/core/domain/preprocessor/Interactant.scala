package core.domain.preprocessor

import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import core.domain.preprocessor.Stage.stageDecoder

sealed trait Interactant

case class IMolecule(molecule: Molecule) extends Interactant
case class ICatalyst(catalyst: Catalyst) extends Interactant
case class IAccelerate(accelerate: ACCELERATE) extends Interactant
case class IProductFrom(productFrom: PRODUCT_FROM) extends Interactant
case class IReagentIn(reagentIn: REAGENT_IN) extends Interactant
case class IReaction(reaction: Reaction) extends Interactant

object Interactant {

  implicit val interactantEncoder: Encoder[Interactant] = Encoder.instance {
    case i: IMolecule     => IMolecule.iMoleculeEncoder(i)
    case c: ICatalyst     => ICatalyst.iCatalystEncoder(c)
    case a: IAccelerate   => IAccelerate.iAccelerateEncoder(a)
    case pf: IProductFrom => IProductFrom.iProductFromEncoder(pf)
    case ri: IReagentIn   => IReagentIn.iReagentInEncoder(ri)
    case r: IReaction     => IReaction.iReactionEncoder(r)
  }

  implicit val interactantDecoder: Decoder[Interactant] = Decoder.instance { cursor =>
    for {
      tag         <- cursor.downField("tag").as[String]
      contents    <- cursor.downField("contents").focus.map(_.as[Json]).getOrElse(Left(DecodingFailure(
                       "Missing contents",
                       cursor.history
                     )))
      interactant <- tag match {
                       case "IMolecule"    => contents.as[Molecule].map(IMolecule.apply)
                       case "ICatalyst"    => contents.as[Catalyst].map(ICatalyst.apply)
                       case "IAccelerate"  => contents.as[ACCELERATE].map(IAccelerate.apply)
                       case "IProductFrom" => contents.as[PRODUCT_FROM].map(IProductFrom.apply)
                       case "IReagentIn"   => contents.as[REAGENT_IN].map(IReagentIn.apply)
                       case "IReaction"    => contents.as[Reaction].map(IReaction.apply)
                       case _              => Left(DecodingFailure(s"Unknown tag: $tag", cursor.history))
                     }
    } yield interactant
  }

  implicit val stageInteractantDecoder: Decoder[List[(Stage, List[Interactant])]] =
    Decoder.decodeList(
      Decoder.instance { cursor =>
        for {
          stage        <- cursor.downArray.as[Stage]
          interactants <- cursor.downArray.right.as[List[Interactant]]
        } yield (stage, interactants)
      }
    )

}

object IMolecule {
  implicit val iMoleculeEncoder: Encoder[IMolecule] = deriveEncoder[IMolecule]
  implicit val iMoleculeDecoder: Decoder[IMolecule] = deriveDecoder[IMolecule]
}

object ICatalyst {
  implicit val iCatalystEncoder: Encoder[ICatalyst] = deriveEncoder[ICatalyst]
  implicit val iCatalystDecoder: Decoder[ICatalyst] = deriveDecoder[ICatalyst]
}

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
