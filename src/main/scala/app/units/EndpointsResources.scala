package app.units

import api.endpoints.preprocessor.PreprocessorEndpoints
import api.endpoints.flow.ReaktoroEndpoints

import cats.effect.{IO, Resource}

import core.services.flow.ReaktoroService
import core.services.preprocessor.{MechanismService, ReactionService}

import org.typelevel.log4cats.Logger

/**
 * Provides resources for managing API endpoint initialisation. This includes endpoints for the preprocessor and
 * Reaktoro services.
 */
object EndpointResources {

  /**
   * Creates a managed resource for the `PreprocessorEndpoints`.
   *
   * @param reactionService
   *   An instance of `ReactionService` used by the endpoints to handle reaction-related operations.
   * @param mechanismService
   *   An instance of `MechanismService` used by the endpoints to handle mechanism-related operations.
   * @param logger
   *   A logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource` encapsulating the `PreprocessorEndpoints` instance. Ensures that the resource is properly
   *   initialised and cleaned up.
   */
  def preprocessorEndpointsResource(
    reactionService: ReactionService[IO],
    mechanismService: MechanismService[IO]
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, PreprocessorEndpoints] =
    Resource.make(
      logger.info("Creating Preprocessor Endpoints") *>
        IO(new PreprocessorEndpoints(reactionService, mechanismService))
    )(endpoints =>
      logger.info("Shutting down Preprocessor Endpoints").handleErrorWith(_ => IO.unit)
    )

  /**
   * Creates a managed resource for the `ReaktoroEndpoints`.
   *
   * @param reaktoroService
   *   An instance of `ReaktoroService` used by the endpoints for handling Reaktoro-related operations.
   * @param logger
   *   A logger instance for logging lifecycle events and errors during the resource's creation and release.
   * @return
   *   A `Resource` encapsulating the `ReaktoroEndpoints` instance. Ensures that the resource is properly initialised
   *   and cleaned up.
   */
  def reaktoroEndpointsResource(
    reaktoroService: ReaktoroService[IO]
  )(
    implicit logger: Logger[IO]
  ): Resource[IO, ReaktoroEndpoints] =
    Resource.make(
      logger.info("Creating Reaktoro Endpoints") *>
        IO(new ReaktoroEndpoints(reaktoroService))
    )(endpoints =>
      logger.info("Shutting down Reaktoro Endpoints").handleErrorWith(_ => IO.unit)
    )

}
