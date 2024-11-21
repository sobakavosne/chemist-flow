/**
 * Provides configuration classes and utilities for loading and managing application settings.
 */
package config

import com.comcast.ip4s.{Host, Port}
import com.typesafe.config.{Config, ConfigFactory}

import org.http4s.Uri

import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.CannotConvert

import java.io.File

import scala.concurrent.duration.FiniteDuration

case class KafkaTopics(
  reactions:  String,
  mechanisms: String
)

object KafkaTopics {

  implicit val kafkaTopicsReader: ConfigReader[KafkaTopics] =
    ConfigReader.forProduct2("reactions", "mechanisms")(KafkaTopics.apply)

}

case class KafkaConfig(
  bootstrapServers: String,
  topic:            KafkaTopics
)

object KafkaConfig {

  implicit val kafkaConfigReader: ConfigReader[KafkaConfig] =
    ConfigReader.forProduct2("bootstrapServers", "topic")(KafkaConfig.apply)

}

case class HttpConfig(
  host: Host,
  port: Port
)

object HttpConfig {

  implicit val hostReader: ConfigReader[Host] = ConfigReader.fromString { str =>
    Host.fromString(str).toRight(CannotConvert(str, "Host", "Invalid host format"))
  }

  implicit val portReader: ConfigReader[Port] = ConfigReader.fromString { str =>
    Port.fromString(str).toRight(CannotConvert(str, "Port", "Invalid port format"))
  }

  implicit val httpConfigReader: ConfigReader[HttpConfig] =
    ConfigReader.forProduct2("host", "port")(HttpConfig.apply)

}

case class DatabaseConfig(
  url:      String,
  user:     String,
  password: String
)

object DatabaseConfig {

  implicit val databaseConfigReader: ConfigReader[DatabaseConfig] =
    ConfigReader.forProduct3("url", "user", "password")(DatabaseConfig.apply)

}

case class ChemistPreprocessorHttpClient(
  baseUri: Uri,
  timeout: HttpClientTimeout,
  retries: Int,
  pool:    HttpClientPool
)

case class HttpClientTimeout(
  connect: FiniteDuration,
  request: FiniteDuration
)

case class HttpClientPool(
  maxConnections: Int,
  maxIdleTime:    FiniteDuration
)

object ChemistPreprocessorHttpClient {

  implicit val httpClientTimeoutReader: ConfigReader[HttpClientTimeout] =
    ConfigReader.forProduct2("connect", "request")(HttpClientTimeout.apply)

  implicit val httpClientPoolReader: ConfigReader[HttpClientPool] =
    ConfigReader.forProduct2("max-connections", "max-idle-time")(HttpClientPool.apply)

  implicit val baseUriReader: ConfigReader[Uri] = ConfigReader.fromString { str =>
    Uri.fromString(str).left.map(failure => CannotConvert(str, "Uri", failure.sanitized))
  }

  implicit val httpClientConfigReader: ConfigReader[ChemistPreprocessorHttpClient] =
    ConfigReader.forProduct4("baseUri", "timeout", "retries", "pool")(ChemistPreprocessorHttpClient.apply)

}

case class ChemistEngineHttpClient(
  baseUri: Uri,
  timeout: HttpClientTimeout,
  retries: Int,
  pool:    HttpClientPool
)

object ChemistEngineHttpClient {

  implicit val httpClientTimeoutReader: ConfigReader[HttpClientTimeout] =
    ConfigReader.forProduct2("connect", "request")(HttpClientTimeout.apply)

  implicit val httpClientPoolReader: ConfigReader[HttpClientPool] =
    ConfigReader.forProduct2("max-connections", "max-idle-time")(HttpClientPool.apply)

  implicit val baseUriReader: ConfigReader[Uri] = ConfigReader.fromString { str =>
    Uri.fromString(str).left.map(failure => CannotConvert(str, "Uri", failure.sanitized))
  }

  implicit val httpClientConfigReader: ConfigReader[ChemistEngineHttpClient] =
    ConfigReader.forProduct4("baseUri", "timeout", "retries", "pool")(ChemistEngineHttpClient.apply)

}

/**
 * Represents the application-wide configuration.
 *
 * @param kafka
 *   The Kafka configuration.
 * @param http
 *   The HTTP server configuration.
 * @param database
 *   The database configuration.
 * @param preprocessorHttpClient
 *   The Chemist preprocessor HTTP client configuration.
 * @param engineHttpClient
 *   The Chemist engine HTTP client configuration.
 */
case class AppConfig(
  kafka:                  KafkaConfig,
  http:                   HttpConfig,
  database:               DatabaseConfig,
  preprocessorHttpClient: ChemistPreprocessorHttpClient,
  engineHttpClient:       ChemistEngineHttpClient
)

object AppConfig {

  implicit val appConfigReader: ConfigReader[AppConfig] =
    ConfigReader.forProduct5(
      "kafka",
      "http",
      "database",
      "chemistPreprocessorHttpClient",
      "chemistEngineHttpClient"
    )(
      AppConfig.apply
    )

}

sealed trait ConfigLoader {
  def appConfig: AppConfig
  def kafkaConfig: KafkaConfig
  def httpConfig: HttpConfig
  def databaseConfig: DatabaseConfig
  def preprocessorHttpClientConfig: ChemistPreprocessorHttpClient
  def engineHttpClientConfig: ChemistEngineHttpClient
}

object ConfigLoader {
  System.setProperty("logback.configurationFile", "src/main/scala/resource/logback.xml")

  private val refConf: Config =
    ConfigFactory.parseFile(new File("src/main/scala/resource/reference.conf"))

  private val appConf: Config =
    ConfigFactory.parseFile(new File("src/main/scala/resource/application.conf"))

  private val config: Config =
    appConf.withFallback(refConf).resolve()

  private lazy val loadedAppConfig: AppConfig = ConfigSource.fromConfig(config).loadOrThrow[AppConfig]

  case object DefaultConfigLoader extends ConfigLoader {
    override val appConfig: AppConfig                                        = loadedAppConfig
    override val kafkaConfig: KafkaConfig                                    = appConfig.kafka
    override val httpConfig: HttpConfig                                      = appConfig.http
    override val databaseConfig: DatabaseConfig                              = appConfig.database
    override val preprocessorHttpClientConfig: ChemistPreprocessorHttpClient = appConfig.preprocessorHttpClient
    override val engineHttpClientConfig: ChemistEngineHttpClient             = appConfig.engineHttpClient

    val pureConfig = config
  }

}
