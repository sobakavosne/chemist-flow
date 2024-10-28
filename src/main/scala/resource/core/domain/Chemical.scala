package chemical

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Molecule(
  moleculeId: Int,
  moleculeSmiles: String,
  moleculeIupacName: String
)

case class Reaction(
  reactionId: Int,
  reactionName: String
)

case class Catalyst(
  catalystId: Int,
  catalystSmiles: String,
  catalystName: Option[String]
)

case class ProductFrom(productAmount: Float)

case class ReagentIn(reagentAmount: Float)

case class Accelerate(
  temperature: List[Float],
  pressure: List[Float]
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

object ProductFrom {
  implicit val productFromEncoder: Encoder[ProductFrom] = deriveEncoder[ProductFrom]
  implicit val productFromDecoder: Decoder[ProductFrom] = deriveDecoder[ProductFrom]
}

object ReagentIn {
  implicit val reagentInEncoder: Encoder[ReagentIn] = deriveEncoder[ReagentIn]
  implicit val reagentInDecoder: Decoder[ReagentIn] = deriveDecoder[ReagentIn]
}

object Accelerate {
  implicit val accelerateEncoder: Encoder[Accelerate] = deriveEncoder[Accelerate]
  implicit val accelerateDecoder: Decoder[Accelerate] = deriveDecoder[Accelerate]
}
