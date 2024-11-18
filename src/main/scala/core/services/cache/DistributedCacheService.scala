// WORK WITH SCALA 2.13

// package core.services

// import akka.actor.{ActorSystem, Scheduler}
// import akka.cluster.ddata.Replicator.{Get, Update, WriteLocal}
// import akka.cluster.ddata.scaladsl.{DistributedData, LWWMapKey, Replicator}
// // import akka.cluster.ddata._
// // import akka.util.Timeout
// import cats.effect.Sync
// import cats.implicits._
// import core.domain.{Mechanism, MechanismId, Reaction, ReactionId}
// import scala.concurrent.duration._
// import cats.effect.kernel.Sync

// class DistributedCacheService[F[_]: Sync](system: ActorSystem) {
//   private val replicator = DistributedData(system).replicator

//   // implicit private val timeout: Timeout     = 3.seconds
//   implicit private val scheduler: Scheduler = system.scheduler

//   // Keys for distributed data maps
//   private val mechanismCacheKey = LWWMapKey[MechanismId, Mechanism]("mechanismCache")
//   private val reactionCacheKey  = LWWMapKey[ReactionId, Reaction]("reactionCache")

//   def getMechanism(id: MechanismId): F[Option[Mechanism]] = Sync[F].delay {
//     val getRequest = Get(mechanismCacheKey, Replicator.readLocal)
//     replicator.ask[Replicator.GetResponse[LWWMapKey[MechanismId, Mechanism]]](replyTo =>
//       getRequest.copy(replyTo = replyTo)
//     )
//       .map {
//         case response: Replicator.GetSuccess[LWWMapKey[MechanismId, Mechanism]] =>
//           response.dataValue.get(id)
//         case _                                                                  => None
//       }
//   }

//   def putMechanism(id: MechanismId, mechanism: Mechanism): F[Unit] = Sync[F].delay {
//     val update = Update(mechanismCacheKey, Replicator.writeLocal)(_ + (id -> mechanism))
//     replicator.ask[Replicator.UpdateResponse[LWWMapKey[MechanismId, Mechanism]]](replyTo =>
//       update.copy(replyTo = replyTo)
//     )
//   }

//   def getReaction(id: ReactionId): F[Option[Reaction]] = Sync[F].delay {
//     val getRequest = Get(reactionCacheKey, Replicator.readLocal)
//     replicator.ask[Replicator.GetResponse[LWWMapKey[ReactionId, Reaction]]](replyTo =>
//       getRequest.copy(replyTo = replyTo)
//     )
//       .map {
//         case response: Replicator.GetSuccess[LWWMapKey[ReactionId, Reaction]] =>
//           response.dataValue.get(id)
//         case _                                                                => None
//       }
//   }

//   def putReaction(id: ReactionId, reaction: Reaction): F[Unit] = Sync[F].delay {
//     val update = Update(reactionCacheKey, Replicator.writeLocal)(_ + (id -> reaction))
//     replicator.ask[Replicator.UpdateResponse[LWWMapKey[ReactionId, Reaction]]](replyTo =>
//       update.copy(replyTo = replyTo)
//     )
//   }

//   def cleanExpiredEntries: F[Unit] = Sync[F].unit
// }
