// package core.services

// import cats.effect.Concurrent
// import core.domain.{Mechanism, MechanismId}
// import core.repositories.types.MechanismRepository
// import core.errors.MechanismError
// import org.http4s.client.Client
// import org.http4s.{Method, Request, Status, Uri}
// import io.circe.syntax._
// import io.circe.generic.auto._
// import org.http4s.circe._
// import org.http4s.implicits._
// import cats.implicits._
// import org.http4s.FormDataDecoder.formEntityDecoder
// import org.http4s.EntityDecoder

// object MechanismService {
//   implicit def mechanismEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, Mechanism] =
//     jsonOf[F, Mechanism]
// }

// class MechanismService[F[_]: Concurrent](client: Client[F], repository: MechanismRepository[F]) {

//   private val baseUri = uri"http://localhost:8080/api/mechanism" // Replace with actual Neo4j endpoint

//   // Retrieve a mechanism by ID
//   def getMechanism(id: MechanismId): F[Either[MechanismError, Mechanism]] =
//     client.run(Request[F](Method.GET, baseUri / id.toString)).use { response =>
//       response.as[Mechanism].attempt.map {
//         case Right(mechanism) if response.status.isSuccess => Right(mechanism)
//         case Right(_)                                      => Left(MechanismError(s"Mechanism not found with ID: $id"))
//         case Left(error) => Left(MechanismError(s"Failed to fetch Mechanism: ${error.getMessage}"))
//       }
//     }

//   // Create a new mechanism
//   def createMechanism(mechanism: Mechanism): F[Either[MechanismError, Mechanism]] =
//     client.run(Request[F](Method.POST, baseUri).withEntity(mechanism.asJson)).use { response =>
//       response.as[Mechanism].attempt.map {
//         case Right(createdMechanism) if response.status.isSuccess => Right(createdMechanism)
//         case Right(_)    => Left(MechanismError("Mechanism could not be created"))
//         case Left(error) => Left(MechanismError(s"Failed to create Mechanism: ${error.getMessage}"))
//       }
//     }

//   // Update a mechanism by ID
//   def updateMechanism(id: MechanismId, mechanism: Mechanism): F[Either[MechanismError, Mechanism]] =
//     client.run(Request[F](Method.PUT, baseUri / id.toString).withEntity(mechanism.asJson)).use { response =>
//       response.as[Mechanism].attempt.map {
//         case Right(updatedMechanism) if response.status.isSuccess => Right(updatedMechanism)
//         case Right(_)    => Left(MechanismError("Mechanism could not be updated"))
//         case Left(error) => Left(MechanismError(s"Failed to update Mechanism: ${error.getMessage}"))
//       }
//     }

//   // Delete a mechanism by ID
//   def deleteMechanism(id: MechanismId): F[Either[MechanismError, Boolean]] =
//     client.run(Request[F](Method.DELETE, baseUri / id.toString)).use { response =>
//       if (response.status == Status.NoContent) Sync[F].pure(Right(true))
//       else Sync[F].pure(Left(MechanismError(s"Failed to delete Mechanism with ID: $id")))
//     }
// }
