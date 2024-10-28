package resource.core.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Interactant(
  id: String,
  name: String,
  role: String
)

object Interactant {
  implicit val interactantEncoder: Encoder[Interactant] = deriveEncoder[Interactant]
  implicit val interactantDecoder: Decoder[Interactant] = deriveDecoder[Interactant]
}
