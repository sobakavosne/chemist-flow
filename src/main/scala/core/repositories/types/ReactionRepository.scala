package core.repositories.types

import core.domain.preprocessor.{Reaction, ReactionId}
import core.errors.http.preprocessor.ReactionError

/**
 * Represents a repository interface for managing reactions in a data store.
 *
 * @tparam F
 *   The effect type (e.g., `IO`, `Future`, etc.).
 */
trait ReactionRepository[F[_]] {

  /**
   * Retrieves a reaction by its ID.
   *
   * @param id
   *   The ID of the reaction to retrieve.
   * @return
   *   An effect wrapping an optional `Reaction`. Returns `None` if the reaction is not found.
   */
  def get(id: ReactionId): F[Option[Reaction]]

  /**
   * Creates a new reaction in the data store.
   *
   * @param reaction
   *   The `Reaction` instance to create.
   * @return
   *   An effect wrapping either a `ReactionError` if the creation fails or the created `Reaction` on success.
   */
  def create(reaction: Reaction): F[Either[ReactionError, Reaction]]

  /**
   * Deletes a reaction by its ID.
   *
   * @param id
   *   The ID of the reaction to delete.
   * @return
   *   An effect wrapping a `Boolean` indicating whether the deletion was successful.
   */
  def delete(id: ReactionId): F[Boolean]
}
