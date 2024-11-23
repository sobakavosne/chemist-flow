package core.services.cache

import cats.effect.Sync
import cats.implicits.{toFlatMapOps, toFunctorOps}

import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId, Reaction, ReactionDetails, ReactionId}
import core.services.cache.types.CacheServiceTrait

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._

/**
 * A local, in-memory service for caching mechanisms and reactions with a time-to-live (TTL) mechanism.
 *
 * This service uses a `TrieMap` for thread-safe, concurrent caching. Each cache entry is timestamped, and expired
 * entries are removed based on the configured TTL. The service provides CRUD operations for mechanisms and reactions,
 * ensuring expired entries are not returned or updated.
 *
 * @param ttl
 *   The time-to-live (TTL) for cache entries. Entries older than this duration are considered expired.
 * @tparam F
 *   The effect type (e.g., `IO`, `SyncIO`, etc.) used to encapsulate computations.
 */
class LocalCacheService[F[_]: Sync](
  implicit ttl: FiniteDuration
) extends CacheServiceTrait[F] {

  private val mechanismCache: TrieMap[MechanismId, (Mechanism, Long)]               = TrieMap.empty
  private val mechanismDetailsCache: TrieMap[MechanismId, (MechanismDetails, Long)] = TrieMap.empty
  private val reactionCache: TrieMap[ReactionId, (Reaction, Long)]                  = TrieMap.empty
  private val reactionDetailsCache: TrieMap[ReactionId, (ReactionDetails, Long)]    = TrieMap.empty

  private def currentTime: Long = System.currentTimeMillis

  private def isExpired(entryTime: Long): Boolean =
    currentTime - entryTime > ttl.toMillis

  private def cleanCache[K, V](cache: TrieMap[K, (V, Long)]): F[Unit] =
    Sync[F].delay { cache.filterInPlace { case (_, (_, timestamp)) => !isExpired(timestamp) } }

  private def getFromCache[K, V](cache: TrieMap[K, (V, Long)], id: K): F[Option[V]] =
    Sync[F].delay {
      cache.get(id).collect {
        case (value, timestamp) if !isExpired(timestamp) => value
      }
    }

  private def putInCache[K, V](cache: TrieMap[K, (V, Long)], id: K, value: V): F[Unit] =
    Sync[F].delay(cache.update(id, (value, currentTime)))

  override def getMechanism(id: MechanismId): F[Option[Mechanism]] =
    getFromCache(mechanismCache, id)

  override def getMechanismDetails(id: MechanismId): F[Option[MechanismDetails]] =
    getFromCache(mechanismDetailsCache, id)

  override def putMechanism(id: MechanismId, mechanism: Mechanism): F[Unit] =
    putInCache(mechanismCache, id, mechanism)

  override def putMechanismDetails(id: MechanismId, mechanismDetails: MechanismDetails): F[Unit] =
    putInCache(mechanismDetailsCache, id, mechanismDetails)

  override def createMechanism(id: MechanismId, mechanism: Mechanism): F[Either[String, Mechanism]] = Sync[F].delay {
    if (mechanismCache.contains(id)) Left(s"Mechanism with ID $id already exists in cache.")
    else {
      mechanismCache.update(id, (mechanism, currentTime))
      Right(mechanism)
    }
  }

  override def getReaction(id: ReactionId): F[Option[Reaction]] =
    getFromCache(reactionCache, id)

  override def getReactionDetails(id: ReactionId): F[Option[ReactionDetails]] =
    getFromCache(reactionDetailsCache, id)

  override def putReaction(id: ReactionId, reaction: Reaction): F[Unit] =
    putInCache(reactionCache, id, reaction)

  override def putReactionDetails(id: ReactionId, reactionDetails: ReactionDetails): F[Unit] =
    putInCache(reactionDetailsCache, id, reactionDetails)

  override def createReaction(id: ReactionId, reaction: Reaction): F[Either[String, Reaction]] = Sync[F].delay {
    if (reactionCache.contains(id)) Left(s"Reaction with ID $id already exists in cache.")
    else {
      reactionCache.update(id, (reaction, currentTime))
      Right(reaction)
    }
  }

  /**
   * Removes expired entries from all caches.
   *
   * This method checks the timestamp of each cache entry and removes entries that have exceeded the configured TTL.
   * This operation is performed in-memory and is thread-safe.
   *
   * @return
   *   An effectful computation that completes when all expired entries have been removed.
   */
  def cleanExpiredEntries: F[Unit] = {
    for {
      _ <- cleanCache(mechanismCache)
      _ <- cleanCache(mechanismDetailsCache)
      _ <- cleanCache(reactionCache)
      _ <- cleanCache(reactionDetailsCache)
    } yield ()
  }

}
