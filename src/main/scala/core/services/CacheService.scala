package core.services

import scala.concurrent.duration._
import core.domain.{Mechanism, MechanismId, Reaction, ReactionId}
import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.FiniteDuration
import cats.effect.kernel.Sync

class CacheService[F[_]: Sync] {
  private val mechanismCache: TrieMap[MechanismId, (Mechanism, Long)] = TrieMap.empty
  private val reactionCache: TrieMap[ReactionId, (Reaction, Long)]    = TrieMap.empty
  private val ttl: FiniteDuration                                     = 5.minutes

  private def currentTime: Long = System.currentTimeMillis

  private def isExpired(entryTime: Long): Boolean = currentTime - entryTime > ttl.toMillis

  def getMechanism(id: MechanismId): F[Option[Mechanism]] = Sync[F].delay {
    mechanismCache.get(id).collect {
      case (mechanism, timestamp) if !isExpired(timestamp) => mechanism
    }
  }

  def putMechanism(id: MechanismId, mechanism: Mechanism): F[Unit] = Sync[F].delay {
    mechanismCache.update(id, (mechanism, currentTime))
  }

  def createMechanism(id: MechanismId, mechanism: Mechanism): F[Either[String, Mechanism]] = Sync[F].delay {
    if (mechanismCache.contains(id)) Left(s"Mechanism with ID $id already exists in cache.")
    else {
      mechanismCache.update(id, (mechanism, currentTime))
      Right(mechanism)
    }
  }

  def getReaction(id: ReactionId): F[Option[Reaction]] = Sync[F].delay {
    reactionCache.get(id).collect {
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

  def cleanExpiredEntries: F[Unit] = Sync[F].delay {
    val now = currentTime
    mechanismCache.filterInPlace { case (_, (_, timestamp)) => now - timestamp <= ttl.toMillis }
    reactionCache.filterInPlace { case (_, (_, timestamp)) => now - timestamp <= ttl.toMillis }
  }

}
