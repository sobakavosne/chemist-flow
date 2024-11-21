package core.services.cache

import akka.actor.ActorSystem
import akka.cluster.ddata.Replicator.{Get, ReadAll, Update, WriteAll}
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey, Replicator, SelfUniqueAddress}
import akka.util.Timeout
import akka.pattern.ask

import cats.effect.Async
import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId, Reaction, ReactionDetails, ReactionId}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * A distributed cache service for managing mechanisms and reactions using Akka Distributed Data.
 *
 * This service provides caching with consistency guarantees across multiple nodes in a cluster.
 *
 * @param system
 *   The ActorSystem for Akka operations.
 * @param selfUniqueAddress
 *   The unique address of the node interacting with the cache.
 * @tparam F
 *   The effect type (e.g., `IO`, `Future`, etc.).
 */
class DistributedCacheService[F[_]: Async](
  system:            ActorSystem,
  selfUniqueAddress: SelfUniqueAddress
) {

  implicit private val timeout: Timeout     = Timeout(5.seconds)
  implicit private val ec: ExecutionContext = system.dispatcher

  private val replicator = DistributedData(system).replicator

  private val mechanismCacheKey        = LWWMapKey[MechanismId, Mechanism]("mechanismCache")
  private val mechanismDetailsCacheKey = LWWMapKey[MechanismId, MechanismDetails]("mechanismDetailsCache")
  private val reactionCacheKey         = LWWMapKey[ReactionId, Reaction]("reactionCache")
  private val reactionDetailsCacheKey  = LWWMapKey[ReactionId, ReactionDetails]("reactionDetailsCache")

  /**
   * Retrieves a mechanism's details from the distributed cache.
   *
   * @param id
   *   The ID of the mechanism.
   * @return
   *   An effect wrapping an optional `MechanismDetails`.
   */
  def getMechanism(id: MechanismId): F[Option[MechanismDetails]] =
    getFromCache(mechanismDetailsCacheKey, id)

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
  def putMechanism(id: MechanismId, mechanism: Mechanism): F[Unit] =
    putInCache(mechanismCacheKey, id, mechanism)

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
  def putMechanismDetails(id: MechanismId, mechanismDetails: MechanismDetails): F[Unit] =
    putInCache(mechanismDetailsCacheKey, id, mechanismDetails)

  /**
   * Retrieves a reaction's details from the distributed cache.
   *
   * @param id
   *   The ID of the reaction.
   * @return
   *   An effect wrapping an optional `ReactionDetails`.
   */
  def getReaction(id: ReactionId): F[Option[ReactionDetails]] =
    getFromCache(reactionDetailsCacheKey, id)

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
  def putReaction(id: ReactionId, reaction: Reaction): F[Unit] =
    putInCache(reactionCacheKey, id, reaction)

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
  def putReactionDetails(id: ReactionId, reactionDetails: ReactionDetails): F[Unit] =
    putInCache(reactionDetailsCacheKey, id, reactionDetails)

  /**
   * Cleans expired entries from the distributed cache (not applicable in a distributed system).
   *
   * @return
   *   An effect indicating completion.
   */
  def cleanExpiredEntries: F[Unit] = Async[F].unit

  private def getFromCache[K, V](key: LWWMapKey[K, V], id: K): F[Option[V]] =
    Async[F].fromFuture {
      Async[F].delay {
        (replicator ? Get(key, ReadAll(5.seconds))).map {
          case response: Replicator.GetSuccess[LWWMap[K, V]] @unchecked =>
            response.dataValue.get(id)
          case _: Replicator.NotFound[V] @unchecked                     =>
            None
          case other                                                    =>
            system.log.warning(s"Unexpected response from cache: $other")
            None
        }
      }
    }

  private def putInCache[K, V](key: LWWMapKey[K, V], id: K, value: V): F[Unit] =
    Async[F].fromFuture {
      Async[F].delay {
        (replicator ? Update(key, LWWMap.empty[K, V], WriteAll(5.seconds)) {
          _.put(selfUniqueAddress, id, value)
        }).map(_ => ())
      }
    }

}
