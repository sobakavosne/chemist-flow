package config

import com.comcast.ip4s.{Host, Port}
import com.typesafe.config.{Config, ConfigFactory}
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.CannotConvert
import java.io.File
import scala.concurrent.duration.FiniteDuration

case class KafkaTopics(
  reactions:  String,
  mechanisms: String
)

object KafkaTopics
implicit val kafkaTopicsReader: ConfigReader[KafkaTopics] =
  ConfigReader.forProduct2("reactions", "mechanisms")(KafkaTopics.apply)

case class KafkaConfig(
  bootstrapServers: String,
  topic:            KafkaTopics
)

object KafkaConfig
implicit val kafkaConfigReader: ConfigReader[KafkaConfig] =
  ConfigReader.forProduct2("bootstrapServers", "topic")(KafkaConfig.apply)

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

object DatabaseConfig
implicit val databaseConfigReader: ConfigReader[DatabaseConfig] =
  ConfigReader.forProduct3("url", "user", "password")(DatabaseConfig.apply)

case class HttpClientConfig(
  baseUri: String,
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

object HttpClientConfig {
  implicit val httpClientTimeoutReader: ConfigReader[HttpClientTimeout] =
    ConfigReader.forProduct2("connect", "request")(HttpClientTimeout.apply)

  implicit val httpClientPoolReader: ConfigReader[HttpClientPool] =
    ConfigReader.forProduct2("max-connections", "max-idle-time")(HttpClientPool.apply)

  implicit val httpClientConfigReader: ConfigReader[HttpClientConfig] =
    ConfigReader.forProduct4("baseUri", "timeout", "retries", "pool")(HttpClientConfig.apply)
}

case class AppConfig(
  kafka:      KafkaConfig,
  http:       HttpConfig,
  database:   DatabaseConfig,
  httpClient: HttpClientConfig
)

object AppConfig
implicit val appConfigReader: ConfigReader[AppConfig] =
  ConfigReader.forProduct4("kafka", "http", "database", "httpClient")(AppConfig.apply)

object ConfigLoader {
  System.setProperty("logback.configurationFile", "src/main/scala/resource/logback.xml")

  private val refConf: Config =
    ConfigFactory.parseFile(new File("src/main/scala/resource/reference.conf"))

  private val appConf: Config =
    ConfigFactory.parseFile(new File("src/main/scala/resource/application.conf"))

  private val config: Config =
    appConf.withFallback(refConf).resolve()

  val appConfig: AppConfig               = ConfigSource.fromConfig(config).loadOrThrow[AppConfig]
  val kafkaConfig: KafkaConfig           = appConfig.kafka
  val httpConfig: HttpConfig             = appConfig.http
  val databaseConfig: DatabaseConfig     = appConfig.database
  val httpClientConfig: HttpClientConfig = appConfig.httpClient
}
