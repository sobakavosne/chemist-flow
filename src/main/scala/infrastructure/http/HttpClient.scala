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

/**
 * A generic HTTP client for making RESTful API requests.
 *
 * @param client
 *   The underlying HTTP client instance.
 * @param baseUri
 *   The base URI for all requests.
 * @tparam F
 *   The effect type (e.g., `IO`, `Future`, etc.).
 */
class HttpClient[F[_]: Async](client: Client[F], baseUri: Uri) extends Http4sClientDsl[F] {

  /**
   * Sends an HTTP request and returns the response as a string.
   *
   * @param request
   *   The HTTP request to send.
   * @return
   *   An effect wrapping the response body as a string. Throws an exception if the response status indicates failure.
   */
  private def sendRequest(request: Request[F]): F[String] =
    client.run(request).use { response =>
      response.as[String].flatMap { body =>
        if (response.status.isSuccess) body.pure[F]
        else Async[F].raiseError(new Exception(s"Request failed: $body"))
      }
    }

  /**
   * Sends an HTTP GET request.
   *
   * @param endpoint
   *   The URI path of the endpoint.
   * @return
   *   An effect wrapping the response body as a string.
   */
  def get(endpoint: Uri.Path): F[String] = {
    val request = Request[F](method = GET, uri = baseUri.withPath(endpoint))
    sendRequest(request)
  }

  /**
   * Sends an HTTP POST request with a JSON payload.
   *
   * @param endpoint
   *   The URI path of the endpoint.
   * @param payload
   *   The JSON payload to send.
   * @tparam T
   *   The type of the payload, which must have a Circe `Encoder` instance.
   * @return
   *   An effect wrapping the response body as a string.
   */
  def post[T: Encoder](endpoint: Uri.Path, payload: T): F[String] = {
    val request = Request[F](method = POST, uri = baseUri.withPath(endpoint))
      .withEntity(payload.asJson)
    sendRequest(request)
  }

  /**
   * Sends an HTTP PUT request with a JSON payload.
   *
   * @param endpoint
   *   The URI path of the endpoint.
   * @param payload
   *   The JSON payload to send.
   * @tparam T
   *   The type of the payload, which must have a Circe `Encoder` instance.
   * @return
   *   An effect wrapping the response body as a string.
   */
  def put[T: Encoder](endpoint: Uri.Path, payload: T): F[String] = {
    val request = Request[F](method = PUT, uri = baseUri.withPath(endpoint))
      .withEntity(payload.asJson)
    sendRequest(request)
  }

  /**
   * Sends an HTTP DELETE request.
   *
   * @param endpoint
   *   The URI path of the endpoint.
   * @return
   *   An effect wrapping the response body as a string.
   */
  def delete(endpoint: Uri.Path): F[String] = {
    val request = Request[F](method = DELETE, uri = baseUri.withPath(endpoint))
    sendRequest(request)
  }

  /**
   * Decodes a JSON response into a specified type.
   *
   * @param response
   *   The response body as a JSON string.
   * @tparam T
   *   The type to decode into, which must have a Circe `Decoder` instance.
   * @return
   *   An effect wrapping the decoded value of type `T`. Throws an exception if the decoding fails.
   */
  def decodeResponse[T: Decoder](response: String): F[T] =
    Async[F].fromEither(decode[T](response).left.map(err => new Exception(s"Decoding failed: $err")))

}

/**
 * Companion object for `HttpClient` providing a factory method.
 */
object HttpClient {

  /**
   * Creates a new `HttpClient` instance.
   *
   * @param client
   *   The underlying HTTP client instance.
   * @param baseUri
   *   The base URI for all requests.
   * @tparam F
   *   The effect type (e.g., `IO`, `Future`, etc.).
   * @return
   *   A new `HttpClient` instance.
   */
  def resource[F[_]: Async](client: Client[F], baseUri: Uri): HttpClient[F] = {
    new HttpClient[F](client, baseUri)
  }

}
