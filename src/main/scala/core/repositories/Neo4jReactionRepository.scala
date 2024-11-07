package core.repositories

import cats.effect.Sync
import infrastructure.http.HttpClient
import io.circe.parser.decode
import cats.implicits.{toFlatMapOps, toFunctorOps}
import org.http4s.Uri
import core.domain.{Reaction, ReactionId}
import types.ReactionRepository
import core.errors.ReactionError
import io.circe.syntax.EncoderOps
import core.errors.ReactionError.CreationError

class Neo4jReactionRepository[F[_]: Sync](client: HttpClient[F]) extends ReactionRepository[F] {

  def get(id: ReactionId): F[Option[Reaction]] =
    client.get(Uri.Path.unsafeFromString(s"/reaction/$id"))
      .map(decode[Option[Reaction]])
      .flatMap(Sync[F].fromEither)

  def create(reaction: Reaction): F[Either[ReactionError, Reaction]] = {
    client.post(Uri.Path.unsafeFromString("/reaction"), reaction.asJson).flatMap { response =>
      io.circe.parser.decode[Reaction](response) match {
        case Right(createdReaction) => Sync[F].pure(Right(createdReaction))
        case Left(_)                => Sync[F].pure(Left(CreationError(s"Failed to create reaction: ${reaction.name}")))
      }
    }
  }

  def update(id: Int, reaction: Reaction): F[Option[Reaction]] =
    client.put(Uri.Path.unsafeFromString(s"/reaction/$id"), reaction)
      .map(decode[Option[Reaction]])
      .flatMap(Sync[F].fromEither)

  def delete(id: Int): F[Boolean] =
    client.delete(Uri.Path.unsafeFromString(s"/reaction/$id"))
      .map(decode[Boolean])
      .flatMap(Sync[F].fromEither)
}
