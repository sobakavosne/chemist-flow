package config

import com.comcast.ip4s.{Host, Port}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pureconfig.error.ConfigReaderException
import pureconfig.ConfigSource

class ConfigLoaderSpec extends AnyWordSpec with Matchers {

  "ConfigLoader" should {

    "load the Kafka configuration correctly" in {
      val kafkaConfig = ConfigLoader.kafkaConfig

      kafkaConfig.bootstrapServers shouldBe "localhost:9092"
      kafkaConfig.topic.reactions shouldBe "reactions-topic"
      kafkaConfig.topic.mechanisms shouldBe "mechanisms-topic"
    }

    "load the HTTP configuration correctly" in {
      val httpConfig = ConfigLoader.httpConfig

      httpConfig.host shouldBe Host.fromString("0.0.0.0").get
      httpConfig.port shouldBe Port.fromInt(8081).get
    }

    "load the Database configuration correctly" in {
      val databaseConfig = ConfigLoader.databaseConfig
      databaseConfig.url shouldBe "jdbc:postgresql://localhost:5432/chemist_db"
      databaseConfig.user shouldBe "chemist_user"
      databaseConfig.password shouldBe "chemist_password"
    }

    "load the entire AppConfig correctly" in {
      val appConfig = ConfigLoader.appConfig

      appConfig.kafka.bootstrapServers shouldBe "localhost:9092"
      appConfig.http.host shouldBe Host.fromString("0.0.0.0").get
      appConfig.http.port shouldBe Port.fromInt(8081).get
      appConfig.database.url shouldBe "jdbc:postgresql://localhost:5432/chemist_db"
      appConfig.database.user shouldBe "chemist_user"
      appConfig.database.password shouldBe "chemist_password"
    }

    "fail gracefully if required configuration is missing" in {
      intercept[ConfigReaderException[AppConfig]] {
        val invalidConfigSource = ConfigSource.string("""{ invalidKey: "invalidValue" }""")

        invalidConfigSource.loadOrThrow[AppConfig]
      }
    }
  }
}
