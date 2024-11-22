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
 * This client provides methods for performing standard HTTP operations (`GET`, `POST`, `PUT`, `DELETE`) and supports
 * JSON encoding/decoding using Circe. Requests are constructed relative to the specified base URI, and responses are
 * returned as effectful computations in the specified effect type.
 *
 * @param client
 *   The underlying HTTP client instance used to send requests and receive responses.
 * @param baseUri
 *   The base URI for all API requests made by this client.
 * @tparam F
 *   The effect type (e.g., `IO`, `Future`, etc.) that supports asynchronous and concurrent computations.
 */
class HttpClient[F[_]: Async](client: Client[F], baseUri: Uri) extends Http4sClientDsl[F] {

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
   * Constructs a GET request relative to the base URI and sends it to the specified endpoint.
   *
   * @param endpoint
   *   The URI path of the endpoint to query.
   * @return
   *   An effectful computation that yields the response body as a string.
   */
  def get(endpoint: Uri.Path): F[String] = {
    val request = Request[F](method = GET, uri = baseUri.withPath(endpoint))
    sendRequest(request)
  }

  /**
   * Sends an HTTP POST request with a JSON payload.
   *
   * Constructs a POST request relative to the base URI and sends it to the specified endpoint. The payload is
   * serialised to JSON using Circe.
   *
   * @param endpoint
   *   The URI path of the endpoint to send the request to.
   * @param payload
   *   The JSON payload to include in the request body.
   * @tparam T
   *   The type of the payload, which must have an implicit Circe `Encoder` instance.
   * @return
   *   An effectful computation that yields the response body as a string.
   */
  def post[T: Encoder](endpoint: Uri.Path, payload: T): F[String] = {
    val request = Request[F](method = POST, uri = baseUri.withPath(endpoint))
      .withEntity(payload.asJson)
    sendRequest(request)
  }

  /**
   * Sends an HTTP PUT request with a JSON payload.
   *
   * Constructs a PUT request relative to the base URI and sends it to the specified endpoint. The payload is serialised
   * to JSON using Circe.
   *
   * @param endpoint
   *   The URI path of the endpoint to send the request to.
   * @param payload
   *   The JSON payload to include in the request body.
   * @tparam T
   *   The type of the payload, which must have an implicit Circe `Encoder` instance.
   * @return
   *   An effectful computation that yields the response body as a string.
   */
  def put[T: Encoder](endpoint: Uri.Path, payload: T): F[String] = {
    val request = Request[F](method = PUT, uri = baseUri.withPath(endpoint))
      .withEntity(payload.asJson)
    sendRequest(request)
  }

  /**
   * Sends an HTTP DELETE request.
   *
   * Constructs a DELETE request relative to the base URI and sends it to the specified endpoint.
   *
   * @param endpoint
   *   The URI path of the endpoint to delete.
   * @return
   *   An effectful computation that yields the response body as a string.
   */
  def delete(endpoint: Uri.Path): F[String] = {
    val request = Request[F](method = DELETE, uri = baseUri.withPath(endpoint))
    sendRequest(request)
  }

  /**
   * Decodes a JSON response into a specified type.
   *
   * Attempts to decode the JSON string into the specified type using Circe. If decoding fails, this method raises an
   * error with the failure details.
   *
   * @param response
   *   The response body as a JSON string.
   * @tparam T
   *   The type to decode into, which must have an implicit Circe `Decoder` instance.
   * @return
   *   An effectful computation that yields the decoded value of type `T`.
   */
  def decodeResponse[T: Decoder](response: String): F[T] =
    Async[F].fromEither(decode[T](response).left.map(err => new Exception(s"Decoding failed: $err")))

}

/**
 * Creates a new `HttpClient` instance.
 *
 * @param client
 *   The underlying HTTP client instance used to send requests and receive responses.
 * @param baseUri
 *   The base URI for all API requests made by the client.
 * @tparam F
 *   The effect type (e.g., `IO`, `Future`, etc.) that supports asynchronous computations.
 * @return
 *   A new `HttpClient` instance configured with the given client and base URI.
 */
object HttpClient {

  /**
   * Creates a new `HttpClient` instance.
   *
   * @param client
   *   The underlying HTTP client instance used to send requests and receive responses.
   * @param baseUri
   *   The base URI for all API requests made by the client.
   * @tparam F
   *   The effect type (e.g., `IO`, `Future`, etc.) that supports asynchronous computations.
   * @return
   *   A new `HttpClient` instance configured with the given client and base URI.
   */
  def resource[F[_]: Async](client: Client[F], baseUri: Uri): HttpClient[F] = {
    new HttpClient[F](client, baseUri)
  }

}
