package core.services.cache

import scala.concurrent.duration._
import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId, Reaction, ReactionDetails, ReactionId}
import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.FiniteDuration
import cats.effect.kernel.Sync

/**
 * A service for caching mechanisms and reactions with a time-to-live (TTL) mechanism.
 *
 * @tparam F
 *   The effect type (e.g., `IO`, `SyncIO`, etc.).
 */
class CacheService[F[_]: Sync] {

  private val mechanismCache: TrieMap[MechanismId, (Mechanism, Long)]               = TrieMap.empty
  private val mechanismDetailsCache: TrieMap[MechanismId, (MechanismDetails, Long)] = TrieMap.empty
  private val reactionCache: TrieMap[ReactionId, (Reaction, Long)]                  = TrieMap.empty
  private val reactionDetailsCache: TrieMap[ReactionId, (ReactionDetails, Long)]    = TrieMap.empty
  private val ttl: FiniteDuration                                                   = 5.minutes

  private def currentTime: Long = System.currentTimeMillis

  private def isExpired(entryTime: Long): Boolean = currentTime - entryTime > ttl.toMillis

  /**
   * Retrieves a mechanism's details from the cache.
   *
   * @param id
   *   The ID of the mechanism.
   * @return
   *   An effect wrapping an optional `MechanismDetails`. Returns `None` if the entry is expired or not found.
   */
  def getMechanism(id: MechanismId): F[Option[MechanismDetails]] = Sync[F].delay {
    mechanismDetailsCache.get(id).collect {
      case (mechanism, timestamp) if !isExpired(timestamp) => mechanism
    }
  }

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
  def putMechanism(id: MechanismId, mechanism: Mechanism): F[Unit] = Sync[F].delay {
    mechanismCache.update(id, (mechanism, currentTime))
  }

  /**
   * Caches a mechanism's details by its ID.
   *
   * @param id
   *   The ID of the mechanism.
   * @param mechanism
   *   The `MechanismDetails` instance to cache.
   * @return
   *   An effect indicating completion.
   */
  def putMechanismDetails(id: MechanismId, mechanism: MechanismDetails): F[Unit] = Sync[F].delay {
    mechanismDetailsCache.update(id, (mechanism, currentTime))
  }

  /**
   * Creates a mechanism in the cache if it doesn't already exist.
   *
   * @param id
   *   The ID of the mechanism.
   * @param mechanism
   *   The `Mechanism` instance to create.
   * @return
   *   An effect wrapping either an error message if the mechanism already exists, or the created mechanism.
   */
  def createMechanism(id: MechanismId, mechanism: Mechanism): F[Either[String, Mechanism]] = Sync[F].delay {
    if (mechanismCache.contains(id)) Left(s"Mechanism with ID $id already exists in cache.")
    else {
      mechanismCache.update(id, (mechanism, currentTime))
      Right(mechanism)
    }
  }

  /**
   * Retrieves a reaction's details from the cache.
   *
   * @param id
   *   The ID of the reaction.
   * @return
   *   An effect wrapping an optional `ReactionDetails`. Returns `None` if the entry is expired or not found.
   */
  def getReaction(id: ReactionId): F[Option[ReactionDetails]] = Sync[F].delay {
    reactionDetailsCache.get(id).collect {
      case (reaction, timestamp) if !isExpired(timestamp) => reaction
    }
  }

  /**
   * Creates a reaction in the cache if it doesn't already exist.
   *
   * @param id
   *   The ID of the reaction.
   * @param reaction
   *   The `Reaction` instance to create.
   * @return
   *   An effect wrapping either an error message if the reaction already exists, or the created reaction.
   */
  def createReaction(id: ReactionId, reaction: Reaction): F[Either[String, Reaction]] = Sync[F].delay {
    if (reactionCache.contains(id)) Left(s"Reaction with ID $id already exists in cache.")
    else {
      reactionCache.update(id, (reaction, currentTime))
      Right(reaction)
    }
  }

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
  def putReaction(id: ReactionId, reaction: Reaction): F[Unit] = Sync[F].delay {
    reactionCache.update(id, (reaction, currentTime))
  }

  /**
   * Caches a reaction's details by its ID.
   *
   * @param id
   *   The ID of the reaction.
   * @param reaction
   *   The `ReactionDetails` instance to cache.
   * @return
   *   An effect indicating completion.
   */
  def putReactionDetails(id: ReactionId, reaction: ReactionDetails): F[Unit] = Sync[F].delay {
    reactionDetailsCache.update(id, (reaction, currentTime))
  }

  /**
   * Cleans expired entries from all caches.
   *
   * @return
   *   An effect indicating completion.
   */
  def cleanExpiredEntries: F[Unit] = Sync[F].delay {
    val now = currentTime
    mechanismCache.filterInPlace { case (_, (_, timestamp)) => now - timestamp <= ttl.toMillis }
    mechanismDetailsCache.filterInPlace { case (_, (_, timestamp)) => now - timestamp <= ttl.toMillis }
    reactionCache.filterInPlace { case (_, (_, timestamp)) => now - timestamp <= ttl.toMillis }
    reactionDetailsCache.filterInPlace { case (_, (_, timestamp)) => now - timestamp <= ttl.toMillis }
  }

}
