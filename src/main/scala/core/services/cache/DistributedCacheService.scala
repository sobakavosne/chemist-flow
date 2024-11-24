package core.services.cache

import akka.actor.ActorSystem
import akka.cluster.ddata.Replicator.{Get, ReadAll, Update, WriteAll}
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey, Replicator, SelfUniqueAddress}
import akka.pattern.ask
import akka.util.Timeout

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFunctorOps}

import com.github.benmanes.caffeine.cache.{Cache, Caffeine}

import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId, Reaction, ReactionDetails, ReactionId}
import core.services.cache.types.CacheServiceTrait

import scala.concurrent.ExecutionContext

import java.util.concurrent.TimeUnit

/**
 * A distributed cache service for managing mechanisms and reactions using Akka Distributed Data.
 *
 * This service provides caching with consistency guarantees across multiple nodes in a cluster. It uses `LWWMap`
 * (Last-Write-Wins Map) for conflict resolution and performs distributed read and write operations with configurable
 * timeouts. Additionally, it leverages a local cache for optimised read-heavy workloads, reducing latency for
 * frequently accessed data.
 *
 * @param system
 *   The ActorSystem used for Akka operations, required to initialise the Distributed Data replicator. It enables
 *   communication and coordination across nodes in the cluster.
 * @param selfUniqueAddress
 *   The unique address of the current node interacting with the cache. This address is used to identify updates and
 *   ensure correct state replication in the `LWWMap`.
 * @param ec
 *   The ExecutionContext used for handling asynchronous operations within the service. Ensures non-blocking execution
 *   of distributed reads and writes.
 * @param distributedTtl
 *   The timeout for distributed operations such as `Get` and `Update` in Akka Distributed Data. This defines how long
 *   the system will wait for responses in a distributed environment.
 * @param localTtlWithUnit
 *   A tuple specifying the expiration duration for entries in the local cache. The tuple consists of an integer value
 *   representing the duration and a `TimeUnit` specifying the unit (e.g., minutes, seconds).
 * @tparam F
 *   The effect type (e.g., `IO`, `Future`, etc.) used to encapsulate asynchronous computations in a functional manner.
 */
class DistributedCacheService[F[_]: Async](
  system:            ActorSystem,
  selfUniqueAddress: SelfUniqueAddress
)(
  implicit
  ec:                ExecutionContext,
  distributedTtl:    Timeout,
  localTtlWithUnit:  Tuple2[Int, TimeUnit]
) extends CacheServiceTrait[F] {

  private val Tuple2(localTtl, timeUnit) = localTtlWithUnit
  private val replicator                 = DistributedData(system).replicator
  private val mechanismCacheKey          = LWWMapKey[MechanismId, Mechanism]("mechanismCache")
  private val mechanismDetailsCacheKey   = LWWMapKey[MechanismId, MechanismDetails]("mechanismDetailsCache")
  private val reactionCacheKey           = LWWMapKey[ReactionId, Reaction]("reactionCache")
  private val reactionDetailsCacheKey    = LWWMapKey[ReactionId, ReactionDetails]("reactionDetailsCache")

  private val localMechanismCache: Cache[MechanismId, Mechanism] =
    Caffeine.newBuilder()
      .expireAfterWrite(localTtl, timeUnit)
      .maximumSize(1000)
      .build()

  private val localMechanismDetailsCache: Cache[MechanismId, MechanismDetails] =
    Caffeine.newBuilder()
      .expireAfterWrite(localTtl, timeUnit)
      .maximumSize(1000)
      .build()

  private val localReactionCache: Cache[ReactionId, Reaction] =
    Caffeine.newBuilder()
      .expireAfterWrite(localTtl, timeUnit)
      .maximumSize(1000)
      .build()

  private val localReactionDetailsCache: Cache[ReactionId, ReactionDetails] =
    Caffeine.newBuilder()
      .expireAfterWrite(localTtl, timeUnit)
      .maximumSize(1000)
      .build()

  override def getMechanism(id: MechanismId): F[Option[Mechanism]] =
    Async[F].delay(Option(localMechanismCache.getIfPresent(id))).flatMap {
      case Some(value) => Async[F].pure(Some(value))
      case None        => getFromCache(mechanismCacheKey, id).flatTap {
          case Some(mechanism) => Async[F].delay(localMechanismCache.put(id, mechanism))
          case None            => Async[F].unit
        }
    }

  override def getMechanismDetails(id: MechanismId): F[Option[MechanismDetails]] =
    Async[F].delay(Option(localMechanismDetailsCache.getIfPresent(id))).flatMap {
      case Some(value) => Async[F].pure(Some(value))
      case None        => getFromCache(mechanismDetailsCacheKey, id).flatTap {
          case Some(details) => Async[F].delay(localMechanismDetailsCache.put(id, details))
          case None          => Async[F].unit
        }
    }

  override def putMechanism(id: MechanismId, mechanism: Mechanism): F[Unit] =
    putInCache(mechanismCacheKey, id, mechanism).flatTap(_ =>
      Async[F].delay(localMechanismCache.put(id, mechanism))
    )

  override def putMechanismDetails(id: MechanismId, mechanismDetails: MechanismDetails): F[Unit] =
    putInCache(mechanismDetailsCacheKey, id, mechanismDetails).flatTap(_ =>
      Async[F].delay(localMechanismDetailsCache.put(id, mechanismDetails))
    )

  override def createMechanism(id: MechanismId, mechanism: Mechanism): F[Either[String, Mechanism]] =
    getMechanism(id).flatMap {
      case Some(_) =>
        Async[F].pure(Left(s"Mechanism with ID $id already exists in the cache."))
      case None    =>
        putMechanism(id, mechanism).map(_ => Right(mechanism))
    }

  override def getReaction(id: ReactionId): F[Option[Reaction]] =
    Async[F].delay(Option(localReactionCache.getIfPresent(id))).flatMap {
      case Some(value) => Async[F].pure(Some(value))
      case None        => getFromCache(reactionCacheKey, id).flatTap {
          case Some(reaction) => Async[F].delay(localReactionCache.put(id, reaction))
          case None           => Async[F].unit
        }
    }

  override def getReactionDetails(id: ReactionId): F[Option[ReactionDetails]] =
    Async[F].delay(Option(localReactionDetailsCache.getIfPresent(id))).flatMap {
      case Some(value) => Async[F].pure(Some(value))
      case None        => getFromCache(reactionDetailsCacheKey, id).flatTap {
          case Some(details) => Async[F].delay(localReactionDetailsCache.put(id, details))
          case None          => Async[F].unit
        }
    }

  override def putReaction(id: ReactionId, reaction: Reaction): F[Unit] =
    putInCache(reactionCacheKey, id, reaction).flatTap(_ =>
      Async[F].delay(localReactionCache.put(id, reaction))
    )

  override def putReactionDetails(id: ReactionId, reactionDetails: ReactionDetails): F[Unit] =
    putInCache(reactionDetailsCacheKey, id, reactionDetails).flatTap(_ =>
      Async[F].delay(localReactionDetailsCache.put(id, reactionDetails))
    )

  override def createReaction(id: ReactionId, reaction: Reaction): F[Either[String, Reaction]] =
    getReaction(id).flatMap {
      case Some(_) =>
        Async[F].pure(Left(s"Reaction with ID $id already exists in the cache."))
      case None    =>
        putReaction(id, reaction).map(_ => Right(reaction))
    }

  override def cleanExpiredEntries: F[Unit] = Async[F].unit

  private def getFromCache[K, V](key: LWWMapKey[K, V], id: K): F[Option[V]] =
    Async[F].fromFuture {
      Async[F].delay {
        (replicator ? Get(key, ReadAll(distributedTtl.duration))).map {
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
        (replicator ? Update(key, LWWMap.empty[K, V], WriteAll(distributedTtl.duration)) {
          _.put(selfUniqueAddress, id, value)
        }).void
      }
    }

}
