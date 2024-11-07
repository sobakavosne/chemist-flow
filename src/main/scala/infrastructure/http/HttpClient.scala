package infrastructure.http

import cats.effect.Async
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps}
import org.http4s.{Request, Uri}
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io.{DELETE, GET, POST, PUT}
import io.circe.{Decoder, Encoder}
import io.circe.syntax.EncoderOps
import io.circe.parser.decode

class HttpClient[F[_]: Async](client: Client[F], baseUri: Uri) extends Http4sClientDsl[F] {
  private def sendRequest(request: Request[F]): F[String] =
    client.run(request).use { response =>
      response.as[String].flatMap { body =>
        if (response.status.isSuccess) body.pure[F]
        else Async[F].raiseError(new Exception(s"Request failed: $body"))
      }
    }

  def get(endpoint: Uri.Path): F[String] = {
    val request = Request[F](method = GET, uri = baseUri.withPath(endpoint))
    sendRequest(request)
  }

  def post[T: Encoder](endpoint: Uri.Path, payload: T): F[String] = {
    val request = Request[F](method = POST, uri = baseUri.withPath(endpoint))
      .withEntity(payload.asJson)
    sendRequest(request)
  }

  def put[T: Encoder](endpoint: Uri.Path, payload: T): F[String] = {
    val request = Request[F](method = PUT, uri = baseUri.withPath(endpoint))
      .withEntity(payload.asJson)
    sendRequest(request)
  }

  def delete(endpoint: Uri.Path): F[String] = {
    val request = Request[F](method = DELETE, uri = baseUri.withPath(endpoint))
    sendRequest(request)
  }

  def decodeResponse[T: Decoder](response: String): F[T] =
    Async[F].fromEither(decode[T](response).left.map(err => new Exception(s"Decoding failed: $err")))
}

object HttpClient {
  def resource[F[_]: Async](client: Client[F], baseUri: Uri): HttpClient[F] = {
    new HttpClient[F](client, baseUri)
  }
}
