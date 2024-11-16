package api.endpoints.flow

import core.services.flow.ReaktoroService
import core.domain.preprocessor.ReactionId
import core.domain.flow.DataBase

class ReaktoroEndpoints(
  reaktoroService: ReaktoroService
) {

  def computeChemicalPropsRoute(
    reactionId: ReactionId,
    temperature: Double,
    pressure: Double,
    database: DataBase
  ) =
    reaktoroService.computeChemicalProps(
      reactionId,
      temperature,
      pressure,
      database
    )

}
