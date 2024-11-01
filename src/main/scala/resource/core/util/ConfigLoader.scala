package resource.core.util

import com.comcast.ip4s.{Host, Port}
import com.typesafe.config.{Config, ConfigFactory}
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.CannotConvert
import java.io.File

case class KafkaTopics(
  reactions: String,
  mechanisms: String
)

object KafkaTopics
implicit val kafkaTopicsReader: ConfigReader[KafkaTopics] =
  ConfigReader.forProduct2("reactions", "mechanisms")(KafkaTopics.apply)

case class KafkaConfig(
  bootstrapServers: String,
  topic: KafkaTopics
)

object KafkaConfig
implicit val kafkaConfigReader: ConfigReader[KafkaConfig] =
  ConfigReader.forProduct2("bootstrapServers", "topic")(KafkaConfig.apply)

case class HttpConfig(
  host: Host,
  port: Port
)

implicit val hostReader: ConfigReader[Host] = ConfigReader.fromString { str =>
  Host.fromString(str).toRight(CannotConvert(str, "Host", "Invalid host format"))
}

implicit val portReader: ConfigReader[Port] = ConfigReader.fromString { str =>
  Port.fromString(str).toRight(CannotConvert(str, "Port", "Invalid port format"))
}

object HttpConfig
implicit val httpConfigReader: ConfigReader[HttpConfig] =
  ConfigReader.forProduct2("host", "port")(HttpConfig.apply)

case class DatabaseConfig(
  url: String,
  user: String,
  password: String
)

object DatabaseConfig
implicit val databaseConfigReader: ConfigReader[DatabaseConfig] =
  ConfigReader.forProduct3("url", "user", "password")(DatabaseConfig.apply)

case class AppConfig(
  kafka: KafkaConfig,
  http: HttpConfig,
  database: DatabaseConfig
)

object AppConfig
implicit val appConfigReader: ConfigReader[AppConfig] =
  ConfigReader.forProduct3("kafka", "http", "database")(AppConfig.apply)

object ConfigLoader {
  private val refConf: Config =
    ConfigFactory.parseFile(new File("src/main/scala/resource/core/configs/reference.conf"))

  private val appConf: Config =
    ConfigFactory.parseFile(new File("src/main/scala/resource/core/configs/application.conf"))

  private val config: Config = appConf.withFallback(refConf).resolve()

  val appConfig: AppConfig           = ConfigSource.fromConfig(config).loadOrThrow[AppConfig]
  val kafkaConfig: KafkaConfig       = appConfig.kafka
  val httpConfig: HttpConfig         = appConfig.http
  val databaseConfig: DatabaseConfig = appConfig.database
}
