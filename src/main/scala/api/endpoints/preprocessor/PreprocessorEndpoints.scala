package api.endpoints.preprocessor

import cats.effect.IO
import cats.syntax.semigroupk.toSemigroupKOps
import io.circe.syntax.EncoderOps
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import core.services.preprocessor.{MechanismService, ReactionService}
import core.domain.preprocessor.{MechanismDetails, Reaction, ReactionDetails}
import core.errors.http.preprocessor.ReactionError

class PreprocessorEndpoints(
  reactionService:  ReactionService[IO],
  mechanismService: MechanismService[IO]
) {

  private val getReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "reaction" / id =>
      validateId(id) match {
        case Some(validId) =>
          reactionService.getReaction(validId).flatMap {
            (reactionDetails: ReactionDetails) => Ok(reactionDetails.asJson)
          }.handleErrorWith {
            case _: ReactionError.NotFoundError => NotFound(("NotFound", s"Reaction with ID $validId not found"))
            case ex                             => InternalServerError(("InternalError", ex.getMessage))
          }
        case None          => BadRequest(("BadRequest", "ID must be an integer"))
      }
  }

  private val postReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "reaction" =>
      req.as[Reaction].flatMap { reaction =>
        reactionService.createReaction(reaction).flatMap {
          createdReaction => Created(createdReaction.asJson)
        }.handleErrorWith {
          case _: ReactionError.CreationError => BadRequest(("CreationError", "Failed to create reaction"))
          case ex                             => InternalServerError(("InternalError", ex.getMessage))
        }
      }
  }

  private val deleteReactionRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case DELETE -> Root / "reaction" / id =>
      validateId(id) match {
        case Some(validId) =>
          reactionService.deleteReaction(validId).flatMap {
            case Right(_)    => NoContent()
            case Left(error) => BadRequest(("DeletionError", error.message))
          }
        case None          => BadRequest(("BadRequest", "ID must be an integer"))
      }
  }

  private val getMechanismRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "mechanism" / id =>
      validateId(id) match {
        case Some(validId) =>
          mechanismService.getMechanism(validId).flatMap {
            (mechanismDetails: MechanismDetails) => Ok(mechanismDetails.asJson)
          }.handleErrorWith {
            case _: ReactionError.NotFoundError => NotFound(("NotFound", s"Mechanism with ID $validId not found"))
            case ex                             => InternalServerError(("InternalError", ex.getMessage))
          }
        case None          => BadRequest(("BadRequest", "ID must be an integer"))
      }
  }

  private def validateId(id: String): Option[Int] = id.toIntOption

  val routes: HttpRoutes[IO] = Logger.httpRoutes(logHeaders = false, logBody = true)(
    Router(
      "/api" -> (getReactionRoute <+> postReactionRoute <+> deleteReactionRoute <+> getMechanismRoute)
    )
  )

}
