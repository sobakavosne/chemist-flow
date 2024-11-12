package core.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

type MechanismId = Int
type StageId     = Int

case class Mechanism(
  mechanismId:               MechanismId,
  mechanismName:             String,
  mechanismType:             String,
  mechanismActivationEnergy: Float
)

object Mechanism {
  implicit val mechanismEncoder: Encoder[Mechanism] = deriveEncoder[Mechanism]
  implicit val mechanismDecoder: Decoder[Mechanism] = deriveDecoder[Mechanism]
}

case class FOLLOW(
  description: String
)

object FOLLOW {
  implicit val followEncoder: Encoder[FOLLOW] = deriveEncoder[FOLLOW]
  implicit val followDecoder: Decoder[FOLLOW] = deriveDecoder[FOLLOW]
}

case class Stage(
  stageOrder:       StageId,
  stageName:        String,
  stageDescription: String,
  stageProducts:    List[String]
)

object Stage {
  implicit val stageEncoder: Encoder[Stage] = deriveEncoder[Stage]
  implicit val stageDecoder: Decoder[Stage] = deriveDecoder[Stage]
}

case class INCLUDE()

object INCLUDE {
  implicit val includeEncoder: Encoder[INCLUDE] = deriveEncoder[INCLUDE]
  implicit val includeDecoder: Decoder[INCLUDE] = deriveDecoder[INCLUDE]
}
