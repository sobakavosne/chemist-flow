package resource.models

import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._
import chemical._
import ChemicalJsonProtocol._

case class ReactionDetails(
  reaction: Reaction,
  inboundReagents: List[(REAGENT_IN, Molecule)],
  outboundProducts: List[(PRODUCT_FROM, Molecule)],
  conditions: List[(ACCELERATE, Catalyst)]
)

object ProcessJsonProtocol {
  implicit val reactionDetailsFormat: RootJsonFormat[ReactionDetails] =
    jsonFormat4(ReactionDetails)
}
