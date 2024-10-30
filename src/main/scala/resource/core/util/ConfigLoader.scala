package resource.core.util

import com.typesafe.config.{Config, ConfigFactory}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import java.io.File

case class KafkaTopics(
  reactions: String,
  mechanisms: String
)

case class KafkaConfig(
  bootstrapServers: String,
  topic: KafkaTopics
)

case class HttpConfig(
  host: String,
  port: Int
)

case class DatabaseConfig(
  url: String,
  user: String,
  password: String
)

case class AppConfig(
  kafka: KafkaConfig,
  http: HttpConfig,
  database: DatabaseConfig
)

object ConfigLoader {

  private val refConf: Config = ConfigFactory.parseFile(new File("src/main/scala/resource/core/configs/reference.conf"))
  private val appConf: Config = ConfigFactory.parseFile(new File("src/main/scala/resource/core/configs/application.conf"))
  private val config: Config  = appConf.withFallback(refConf).resolve()

  val appConfig: AppConfig           = ConfigSource.fromConfig(config).loadOrThrow[AppConfig]
  val kafkaConfig: KafkaConfig       = appConfig.kafka
  val httpConfig: HttpConfig         = appConfig.http
  val databaseConfig: DatabaseConfig = appConfig.database
}
