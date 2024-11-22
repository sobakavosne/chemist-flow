package core.services.cache.types

import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId, Reaction, ReactionDetails, ReactionId}

/**
 * A trait defining caching operations for mechanisms and reactions.
 *
 * @tparam F
 *   The effect type (e.g., `IO`, `SyncIO`, etc.).
 */
trait CacheServiceTrait[F[_]] {

  // Mechanism-related methods

  /**
   * Retrieves a mechanism from the cache.
   *
   * @param id
   *   The ID of the mechanism.
   * @return
   *   An effect wrapping an optional `Mechanism`.
   */
  def getMechanism(id: MechanismId): F[Option[Mechanism]]

  /**
   * Retrieves a mechanism's details from the cache.
   *
   * @param id
   *   The ID of the mechanism.
   * @return
   *   An effect wrapping an optional `MechanismDetails`.
   */
  def getMechanismDetails(id: MechanismId): F[Option[MechanismDetails]]

  /**
   * Caches a mechanism by its ID.
   *
   * @param id
   *   The ID of the mechanism.
   * @param mechanism
   *   The `Mechanism` instance to cache.
   * @return
   *   An effect indicating completion.
   */
  def putMechanism(id: MechanismId, mechanism: Mechanism): F[Unit]

  /**
   * Caches a mechanism's details by its ID.
   *
   * @param id
   *   The ID of the mechanism.
   * @param mechanismDetails
   *   The `MechanismDetails` instance to cache.
   * @return
   *   An effect indicating completion.
   */
  def putMechanismDetails(id: MechanismId, mechanismDetails: MechanismDetails): F[Unit]

  /**
   * Creates a mechanism in the cache if it doesn't already exist.
   *
   * @param id
   *   The ID of the mechanism.
   * @param mechanism
   *   The `Mechanism` instance to create.
   * @return
   *   An effect wrapping either an error message if the mechanism exists, or the created mechanism.
   */
  def createMechanism(id: MechanismId, mechanism: Mechanism): F[Either[String, Mechanism]]

  // Reaction-related methods

  /**
   * Retrieves a reaction from the cache.
   *
   * @param id
   *   The ID of the reaction.
   * @return
   *   An effect wrapping an optional `Reaction`.
   */
  def getReaction(id: ReactionId): F[Option[Reaction]]

  /**
   * Retrieves a reaction's details from the cache.
   *
   * @param id
   *   The ID of the reaction.
   * @return
   *   An effect wrapping an optional `ReactionDetails`.
   */
  def getReactionDetails(id: ReactionId): F[Option[ReactionDetails]]

  /**
   * Caches a reaction by its ID.
   *
   * @param id
   *   The ID of the reaction.
   * @param reaction
   *   The `Reaction` instance to cache.
   * @return
   *   An effect indicating completion.
   */
  def putReaction(id: ReactionId, reaction: Reaction): F[Unit]

  /**
   * Caches a reaction's details by its ID.
   *
   * @param id
   *   The ID of the reaction.
   * @param reactionDetails
   *   The `ReactionDetails` instance to cache.
   * @return
   *   An effect indicating completion.
   */
  def putReactionDetails(id: ReactionId, reactionDetails: ReactionDetails): F[Unit]

  /**
   * Creates a reaction in the cache if it doesn't already exist.
   *
   * @param id
   *   The ID of the reaction.
   * @param reaction
   *   The `Reaction` instance to create.
   * @return
   *   An effect wrapping either an error message if the reaction exists, or the created reaction.
   */
  def createReaction(id: ReactionId, reaction: Reaction): F[Either[String, Reaction]]

  // Maintenance methods

  /**
   * Cleans expired entries from the cache.
   *
   * @return
   *   An effect indicating completion.
   */
  def cleanExpiredEntries: F[Unit]

}
