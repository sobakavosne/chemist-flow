package core.services.cache

import scala.concurrent.duration._
import core.domain.preprocessor.{Mechanism, MechanismDetails, MechanismId, Reaction, ReactionDetails, ReactionId}
import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.FiniteDuration
import cats.effect.kernel.Sync

class CacheService[F[_]: Sync] {
  private val mechanismCache: TrieMap[MechanismId, (Mechanism, Long)]               = TrieMap.empty
  private val mechanismDetailsCache: TrieMap[MechanismId, (MechanismDetails, Long)] = TrieMap.empty
  private val reactionCache: TrieMap[ReactionId, (Reaction, Long)]                  = TrieMap.empty
  private val reactionDetailsCache: TrieMap[ReactionId, (ReactionDetails, Long)]    = TrieMap.empty
  private val ttl: FiniteDuration                                                   = 5.minutes

  private def currentTime: Long = System.currentTimeMillis

  private def isExpired(entryTime: Long): Boolean = currentTime - entryTime > ttl.toMillis

  def getMechanism(id: MechanismId): F[Option[MechanismDetails]] = Sync[F].delay {
    mechanismDetailsCache.get(id).collect {
      case (mechanism, timestamp) if !isExpired(timestamp) => mechanism
    }
  }

  def putMechanism(id: MechanismId, mechanism: Mechanism): F[Unit] = Sync[F].delay {
    mechanismCache.update(id, (mechanism, currentTime))
  }

  def putMechanismDetails(id: MechanismId, mechanism: MechanismDetails): F[Unit] = Sync[F].delay {
    mechanismDetailsCache.update(id, (mechanism, currentTime))
  }

  def createMechanism(id: MechanismId, mechanism: Mechanism): F[Either[String, Mechanism]] = Sync[F].delay {
    if (mechanismCache.contains(id)) Left(s"Mechanism with ID $id already exists in cache.")
    else {
      mechanismCache.update(id, (mechanism, currentTime))
      Right(mechanism)
    }
  }

  def getReaction(id: ReactionId): F[Option[ReactionDetails]] = Sync[F].delay {
    reactionDetailsCache.get(id).collect {
      case (reaction, timestamp) if !isExpired(timestamp) => reaction
    }
  }

  def createReaction(id: ReactionId, reaction: Reaction): F[Either[String, Reaction]] = Sync[F].delay {
    if (reactionCache.contains(id)) Left(s"Reaction with ID $id already exists in cache.")
    else {
      reactionCache.update(id, (reaction, currentTime))
      Right(reaction)
    }
  }

  def putReaction(id: ReactionId, reaction: Reaction): F[Unit] = Sync[F].delay {
    reactionCache.update(id, (reaction, currentTime))
  }

  def putReactionDetails(id: ReactionId, reaction: ReactionDetails): F[Unit] = Sync[F].delay {
    reactionDetailsCache.update(id, (reaction, currentTime))
  }

  def cleanExpiredEntries: F[Unit] = Sync[F].delay {
    val now = currentTime
    mechanismCache.filterInPlace { case (_, (_, timestamp)) => now - timestamp <= ttl.toMillis }
    reactionCache.filterInPlace { case (_, (_, timestamp)) => now - timestamp <= ttl.toMillis }
  }

}
