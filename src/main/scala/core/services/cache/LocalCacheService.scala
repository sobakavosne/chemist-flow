package core.services.cache

import scala.concurrent.duration._
import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId, Reaction, ReactionDetails, ReactionId}
import scala.collection.concurrent.TrieMap
import cats.effect.Sync
import core.services.cache.types.CacheServiceTrait

/**
 * A local, in-memory service for caching mechanisms and reactions with a time-to-live (TTL) mechanism.
 *
 * @tparam F
 *   The effect type (e.g., `IO`, `SyncIO`, etc.).
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

  private def cleanCache[K, V](cache: TrieMap[K, (V, Long)]): Unit =
    cache.filterInPlace { case (_, (_, timestamp)) => !isExpired(timestamp) }

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

  def cleanExpiredEntries: F[Unit] = Sync[F].delay {
    cleanCache(mechanismCache)
    cleanCache(mechanismDetailsCache)
    cleanCache(reactionCache)
    cleanCache(reactionDetailsCache)
  }

}
