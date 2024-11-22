package core.services.cache

import akka.actor.ActorSystem
import akka.cluster.ddata.Replicator.{Get, ReadAll, Update, WriteAll}
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey, Replicator, SelfUniqueAddress}
import akka.pattern.ask
import akka.util.Timeout

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFunctorOps}

import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId, Reaction, ReactionDetails, ReactionId}
import core.services.cache.types.CacheServiceTrait

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * A distributed cache service for managing mechanisms and reactions using Akka Distributed Data.
 *
 * This service provides caching with consistency guarantees across multiple nodes in a cluster. It uses `LWWMap`
 * (Last-Write-Wins Map) for conflict resolution and performs distributed read and write operations with configurable
 * timeouts.
 *
 * @param system
 *   The ActorSystem for Akka operations, required to initialise the Distributed Data replicator.
 * @param selfUniqueAddress
 *   The unique address of the node interacting with the cache.
 * @param ec
 *   The ExecutionContext for handling asynchronous operations within the service.
 * @param ttl
 *   The Timeout for distributed operations like `Get` and `Update`.
 * @tparam F
 *   The effect type (e.g., `IO`, `Future`, etc.) used to encapsulate asynchronous computations.
 */
class DistributedCacheService[F[_]: Async](
  system:            ActorSystem,
  selfUniqueAddress: SelfUniqueAddress
)(
  implicit
  ec:                ExecutionContext,
  ttl:               Timeout
) extends CacheServiceTrait[F] {

  private val replicator               = DistributedData(system).replicator
  private val mechanismCacheKey        = LWWMapKey[MechanismId, Mechanism]("mechanismCache")
  private val mechanismDetailsCacheKey = LWWMapKey[MechanismId, MechanismDetails]("mechanismDetailsCache")
  private val reactionCacheKey         = LWWMapKey[ReactionId, Reaction]("reactionCache")
  private val reactionDetailsCacheKey  = LWWMapKey[ReactionId, ReactionDetails]("reactionDetailsCache")

  override def getMechanism(id: MechanismId): F[Option[Mechanism]] =
    getFromCache(mechanismCacheKey, id)

  override def getMechanismDetails(id: MechanismId): F[Option[MechanismDetails]] =
    getFromCache(mechanismDetailsCacheKey, id)

  override def putMechanism(id: MechanismId, mechanism: Mechanism): F[Unit] =
    putInCache(mechanismCacheKey, id, mechanism)

  override def putMechanismDetails(id: MechanismId, mechanismDetails: MechanismDetails): F[Unit] =
    putInCache(mechanismDetailsCacheKey, id, mechanismDetails)

  override def createMechanism(id: MechanismId, mechanism: Mechanism): F[Either[String, Mechanism]] =
    getMechanism(id).flatMap {
      case Some(_) => Async[F].pure(Left(s"Mechanism with ID $id already exists in cache."))
      case None    => putMechanism(id, mechanism).map(_ => Right(mechanism))
    }

  override def getReaction(id: ReactionId): F[Option[Reaction]] =
    getFromCache(reactionCacheKey, id)

  override def getReactionDetails(id: ReactionId): F[Option[ReactionDetails]] =
    getFromCache(reactionDetailsCacheKey, id)

  override def putReaction(id: ReactionId, reaction: Reaction): F[Unit] =
    putInCache(reactionCacheKey, id, reaction)

  override def putReactionDetails(id: ReactionId, reactionDetails: ReactionDetails): F[Unit] =
    putInCache(reactionDetailsCacheKey, id, reactionDetails)

  override def createReaction(id: ReactionId, reaction: Reaction): F[Either[String, Reaction]] =
    getReaction(id).flatMap {
      case Some(_) => Async[F].pure(Left(s"Reaction with ID $id already exists in cache."))
      case None    => putReaction(id, reaction).map(_ => Right(reaction))
    }

  override def cleanExpiredEntries: F[Unit] = Async[F].unit // Not applicable in distributed systems

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
