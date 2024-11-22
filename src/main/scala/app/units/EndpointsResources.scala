package app.units

import api.endpoints.preprocessor.PreprocessorEndpoints
import api.endpoints.flow.ReaktoroEndpoints

import cats.effect.{IO, Resource}

import core.services.flow.ReaktoroService
import core.services.preprocessor.{MechanismService, ReactionService}

import org.typelevel.log4cats.Logger

/**
 * Provides managed resources for API endpoint initialisation.
 *
 * This object handles the creation and lifecycle management of API endpoints, such as those for preprocessor services
 * and Reaktoro services. By encapsulating endpoint initialisation in `Resource`, it ensures proper setup and teardown
 * of these components.
 */
object EndpointResources {

  /**
   * Creates a managed resource for the `PreprocessorEndpoints`.
   *
   * This method initialises and manages the lifecycle of the `PreprocessorEndpoints` instance, which handles API routes
   * for preprocessor-related services, including reactions and mechanisms. It logs lifecycle events during the
   * resource's creation and release for debugging and monitoring purposes.
   *
   * Example usage:
   * {{{
   *   val preprocessorResource = EndpointResources.preprocessorEndpointsResource(
   *     reactionService,
   *     mechanismService
   *   )
   *   preprocessorResource.use { endpoints =>
   *     // Use the endpoints to serve HTTP routes
   *   }
   * }}}
   *
   * @param reactionService
   *   An instance of `ReactionService` for handling reaction-related operations.
   * @param mechanismService
   *   An instance of `MechanismService` for handling mechanism-related operations.
   * @param logger
   *   A logger instance for logging lifecycle events.
   * @return
   *   A `Resource` encapsulating the `PreprocessorEndpoints` instance, ensuring proper initialisation and cleanup.
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
      logger
        .info("Shutting down Preprocessor Endpoints")
        .handleErrorWith(_ => IO.unit)
    )

  /**
   * Creates a managed resource for the `ReaktoroEndpoints`.
   *
   * This method initialises and manages the lifecycle of the `ReaktoroEndpoints` instance, which handles API routes for
   * Reaktoro-related services. Lifecycle events are logged for better observability during resource creation and
   * release.
   *
   * Example usage:
   * {{{
   *   val reaktoroResource = EndpointResources.reaktoroEndpointsResource(reaktoroService)
   *   reaktoroResource.use { endpoints =>
   *     // Use the endpoints to serve HTTP routes
   *   }
   * }}}
   *
   * @param reaktoroService
   *   An instance of `ReaktoroService` for handling Reaktoro-related operations.
   * @param logger
   *   A logger instance for logging lifecycle events.
   * @return
   *   A `Resource` encapsulating the `ReaktoroEndpoints` instance, ensuring proper initialisation and cleanup.
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
      logger
        .info("Shutting down Reaktoro Endpoints")
        .handleErrorWith(_ => IO.unit)
    )

}
