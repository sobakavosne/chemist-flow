// import akka.actor.ActorSystem
// import akka.cluster.{Cluster, MemberStatus}
// import akka.cluster.ddata.{DistributedData, SelfUniqueAddress}
// import akka.testkit.TestKit

// import cats.effect.IO
// import cats.effect.unsafe.implicits.global

// // import com.typesafe.config.ConfigFactory
// import com.typesafe.config.Config

// import config.ConfigLoader.DefaultConfigLoader

// import core.domain.preprocessor.{Mechanism, MechanismId, Reaction, ReactionId}
// import core.services.cache.DistributedCacheService

// import org.scalatest.BeforeAndAfterAll
// import org.scalatest.funsuite.AnyFunSuite
// import org.scalatest.matchers.should.Matchers

// import scala.concurrent.duration._

// class DistributedCacheServiceTest extends AnyFunSuite with Matchers with BeforeAndAfterAll {

//   // val config = ConfigFactory.parseString(s"""
//   //     akka.actor.provider = "cluster"
//   //     akka.remote.artery.canonical.port = 2001
//   //     akka.remote.artery.canonical.hostname = "localhost"
//   //     akka.cluster.seed-nodes = [
//   //       "akka://TestActorSystem@127.0.0.1:2001"
//   //     ]
//   //     """).withFallback(ConfigFactory.load())

//   val config: Config                            = DefaultConfigLoader.pureConfig
//   val system: ActorSystem                       = ActorSystem("ChemistFlowActorSystem", config)
//   val selfUniqueAddress: SelfUniqueAddress      = DistributedData(system).selfUniqueAddress
//   val cacheService: DistributedCacheService[IO] = new DistributedCacheService[IO](system, selfUniqueAddress)
//   val cluster: Cluster                          = Cluster(system)

//   cluster.registerOnMemberUp {
//     println("Cluster is fully operational")
//   }

//   def ensureClusterIsReady(): Unit = {
//     val timeout = System.currentTimeMillis() + 5000
//     while (System.currentTimeMillis() < timeout && !cluster.state.members.exists(_.status == MemberStatus.Up)) {
//       Thread.sleep(100)
//     }
//     if (!cluster.state.members.exists(_.status == MemberStatus.Up)) {
//       throw new RuntimeException("Cluster did not stabilise")
//     }
//   }

//   override def afterAll(): Unit = {
//     TestKit.shutdownActorSystem(system)
//   }

//   override protected def beforeAll(): Unit = {
//     ensureClusterIsReady()
//   }

//   test("putMechanism and getMechanism should store and retrieve a mechanism") {
//     val mechanismId: MechanismId = 1
//     val mechanism: Mechanism     = Mechanism(mechanismId, "mechanism", "type", 1.0)

//     val result = for {
//       _         <- cacheService.putMechanism(mechanismId, mechanism)
//       retrieved <- retryIO(10, 500.millis) {
//                      cacheService.getMechanism(mechanismId).flatMap {
//                        case Some(value) => IO.pure(value)
//                        case None        => IO.raiseError(new Exception("Mechanism not yet replicated"))
//                      }
//                    }
//     } yield retrieved

//     val retrievedMechanism = result.unsafeRunSync()

//     retrievedMechanism shouldEqual Some(mechanism)
//   }

//   test("putReaction and getReaction should store and retrieve a reaction") {
//     val reactionId: ReactionId = 1
//     val reaction: Reaction     = Reaction(reactionId, "reaction")

//     val result = for {
//       _         <- cacheService.putReaction(reactionId, reaction)
//       retrieved <- retryIO(10, 500.millis) {
//                      cacheService.getReaction(reactionId).flatMap {
//                        case Some(value) => IO.pure(value)
//                        case None        => IO.raiseError(new Exception("Reaction not yet replicated"))
//                      }
//                    }
//     } yield retrieved

//     val retrievedReaction = result.unsafeRunSync()

//     retrievedReaction shouldEqual Some(reaction)
//   }

//   private def retryIO[A](retries: Int, delay: FiniteDuration)(action: IO[A]): IO[A] = {
//     action.handleErrorWith { error =>
//       if (retries > 0) IO.sleep(delay) *> retryIO(retries - 1, delay)(action)
//       else IO.raiseError(error)
//     }
//   }

// }
