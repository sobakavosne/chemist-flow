package chemical

import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._

case class Molecule(
  moleculeId: Int,
  moleculeSmiles: String,
  moleculeIupacName: String
)

case class Reaction(reactionId: Int, reactionName: String)

case class Catalyst(
  catalystId: Int,
  catalystSmiles: String,
  catalystName: Option[String]
)

case class PRODUCT_FROM(productAmount: Float)

case class REAGENT_IN(reagentAmount: Float)

case class ACCELERATE(temperature: List[Float], pressure: List[Float])

object ChemicalJsonProtocol {
  implicit val moleculeFormat: RootJsonFormat[Molecule] = jsonFormat3(Molecule)
  implicit val reactionFormat: RootJsonFormat[Reaction] = jsonFormat2(Reaction)
  implicit val catalystFormat: RootJsonFormat[Catalyst] = jsonFormat3(Catalyst)
  implicit val productFromFormat: RootJsonFormat[PRODUCT_FROM] = jsonFormat1(
    PRODUCT_FROM
  )
  implicit val reagentInFormat: RootJsonFormat[REAGENT_IN] = jsonFormat1(
    REAGENT_IN
  )
  implicit val accelerateFormat: RootJsonFormat[ACCELERATE] = jsonFormat2(
    ACCELERATE
  )
}
