package core.domain.preprocessor

import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

/**
 * Represents an interactant in a reaction or mechanism, encoded as various case classes.
 */
sealed trait Interactant

/**
 * Represents a molecule as an interactant.
 *
 * @param molecule
 *   The `Molecule` instance associated with the interactant.
 */
case class IMolecule(molecule: Molecule) extends Interactant

/**
 * Represents a catalyst as an interactant.
 *
 * @param catalyst
 *   The `Catalyst` instance associated with the interactant.
 */
case class ICatalyst(catalyst: Catalyst) extends Interactant

/**
 * Represents acceleration conditions as an interactant.
 *
 * @param accelerate
 *   The `ACCELERATE` instance associated with the interactant.
 */
case class IAccelerate(accelerate: ACCELERATE) extends Interactant

/**
 * Represents a product's formation as an interactant.
 *
 * @param productFrom
 *   The `PRODUCT_FROM` instance associated with the interactant.
 */
case class IProductFrom(productFrom: PRODUCT_FROM) extends Interactant

/**
 * Represents a reagent as an interactant.
 *
 * @param reagentIn
 *   The `REAGENT_IN` instance associated with the interactant.
 */
case class IReagentIn(reagentIn: REAGENT_IN) extends Interactant

/**
 * Represents a reaction as an interactant.
 *
 * @param reaction
 *   The `Reaction` instance associated with the interactant.
 */
case class IReaction(reaction: Reaction) extends Interactant

object Interactant {

  implicit val interactantEncoder: Encoder[Interactant] = Encoder.instance {
    case i: IMolecule     => IMolecule.encoder(i)
    case c: ICatalyst     => ICatalyst.encoder(c)
    case a: IAccelerate   => IAccelerate.encoder(a)
    case pf: IProductFrom => IProductFrom.encoder(pf)
    case ri: IReagentIn   => IReagentIn.encoder(ri)
    case r: IReaction     => IReaction.encoder(r)
  }

  implicit val interactantDecoder: Decoder[Interactant] = Decoder.instance { cursor =>
    for {
      tag         <- cursor.downField("tag").as[String]
      contents    <- cursor.downField("contents").focus.map(_.as[Json]).getOrElse(
                       Left(DecodingFailure("Missing contents", cursor.history))
                     )
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
  implicit val encoder: Encoder[IMolecule] = deriveEncoder[IMolecule]
  implicit val decoder: Decoder[IMolecule] = deriveDecoder[IMolecule]
}

object ICatalyst {
  implicit val encoder: Encoder[ICatalyst] = deriveEncoder[ICatalyst]
  implicit val decoder: Decoder[ICatalyst] = deriveDecoder[ICatalyst]
}

object IAccelerate {
  implicit val encoder: Encoder[IAccelerate] = deriveEncoder
  implicit val decoder: Decoder[IAccelerate] = deriveDecoder
}

object IProductFrom {
  implicit val encoder: Encoder[IProductFrom] = deriveEncoder
  implicit val decoder: Decoder[IProductFrom] = deriveDecoder
}

object IReagentIn {
  implicit val encoder: Encoder[IReagentIn] = deriveEncoder
  implicit val decoder: Decoder[IReagentIn] = deriveDecoder
}

object IReaction {
  implicit val encoder: Encoder[IReaction] = deriveEncoder
  implicit val decoder: Decoder[IReaction] = deriveDecoder
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
