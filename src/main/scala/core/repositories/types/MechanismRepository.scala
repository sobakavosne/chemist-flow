package core.repositories.types

import core.domain.preprocessor.{Mechanism, MechanismId}
import core.errors.http.preprocessor.MechanismError

/**
 * Represents a repository interface for managing mechanisms in a data store.
 *
 * @tparam F
 *   The effect type (e.g., `IO`, `Future`, etc.).
 */
trait MechanismRepository[F[_]] {

  /**
   * Retrieves a mechanism by its ID.
   *
   * @param id
   *   The ID of the mechanism to retrieve.
   * @return
   *   An effect wrapping an optional `Mechanism`. Returns `None` if the mechanism is not found.
   */
  def get(id: MechanismId): F[Option[Mechanism]]

  /**
   * Creates a new mechanism in the data store.
   *
   * @param mechanism
   *   The `Mechanism` instance to create.
   * @return
   *   An effect wrapping either a `MechanismError` if the creation fails or the created `Mechanism` on success.
   */
  def create(mechanism: Mechanism): F[Either[MechanismError, Mechanism]]

  /**
   * Deletes a mechanism by its ID.
   *
   * @param id
   *   The ID of the mechanism to delete.
   * @return
   *   An effect wrapping a `Boolean` indicating whether the deletion was successful.
   */
  def delete(id: MechanismId): F[Boolean]
}
