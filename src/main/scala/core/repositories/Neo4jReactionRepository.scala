package core.repositories

import cats.effect.Sync
import cats.implicits.{toFlatMapOps, toFunctorOps}
import core.domain.{Reaction, ReactionId}
import core.errors.http.ReactionError
import core.errors.http.ReactionError.CreationError
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import infrastructure.http.HttpClient
import org.http4s.Uri
import types.ReactionRepository

/**
 * ADDITIONAL MODULE
 *
 * Neo4jReactionRepository provides a direct interface to the Chemist service for managing reactions. This
 * implementation bypasses any caching or additional service logic, directly interacting with the Neo4j-backed Chemist
 * service through HTTP requests.
 *
 * @param client
 *   The HttpClient used to communicate with the Chemist service.
 */
class Neo4jReactionRepository[F[_]: Sync](client: HttpClient[F]) extends ReactionRepository[F] {

  /**
   * Fetches a reaction by ID from the Chemist service.
   *
   * @param id
   *   The ReactionId of the reaction to fetch.
   * @return
   *   An F-wrapped Option of Reaction. If the reaction is found, it returns Some(Reaction), otherwise None.
   */
  def get(id: ReactionId): F[Option[Reaction]] =
    client
      .get(Uri.Path.unsafeFromString(s"/reaction/$id"))
      .map(decode[Option[Reaction]])
      .flatMap(Sync[F].fromEither)

  /**
   * Creates a new reaction in the Chemist service.
   *
   * @param reaction
   *   The Reaction object to be created.
   * @return
   *   An F-wrapped Either with ReactionError on the left in case of a failure, or the created Reaction on the right if
   *   successful.
   */
  def create(reaction: Reaction): F[Either[ReactionError, Reaction]] =
    client
      .post(Uri.Path.unsafeFromString("/reaction"), reaction.asJson)
      .flatMap { response =>
        io.circe.parser.decode[Reaction](response) match {
          case Right(createdReaction) => Sync[F].pure(Right(createdReaction))
          case Left(_) => Sync[F].pure(Left(CreationError(s"Failed to create reaction: ${reaction.name}")))
        }
      }

  /**
   * Updates an existing reaction by ID in the Chemist service.
   *
   * @param id
   *   The ID of the reaction to update.
   * @param reaction
   *   The updated Reaction object.
   * @return
   *   An F-wrapped Option of Reaction. Returns Some(updatedReaction) if successful, otherwise None if the reaction does
   *   not exist or update fails.
   */
  def update(id: Int, reaction: Reaction): F[Option[Reaction]] =
    client
      .put(Uri.Path.unsafeFromString(s"/reaction/$id"), reaction)
      .map(decode[Option[Reaction]])
      .flatMap(Sync[F].fromEither)

  /**
   * Deletes a reaction by ID from the Chemist service.
   *
   * @param id
   *   The ID of the reaction to delete.
   * @return
   *   An F-wrapped Boolean. Returns true if deletion is successful, false otherwise.
   */
  def delete(id: Int): F[Boolean] =
    client
      .delete(Uri.Path.unsafeFromString(s"/reaction/$id"))
      .map(decode[Boolean])
      .flatMap(Sync[F].fromEither)

}
