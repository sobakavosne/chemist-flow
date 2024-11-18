package config

import com.comcast.ip4s.{Host, Port}
import org.http4s.Uri
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pureconfig.error.ConfigReaderException
import pureconfig.ConfigSource
import config.ConfigLoader.DefaultConfigLoader
import scala.concurrent.duration._

class ConfigLoaderSpec extends AnyWordSpec with Matchers {

  "ConfigLoader" should {

    "load the Kafka configuration correctly" in {
      val kafkaConfig = DefaultConfigLoader.kafkaConfig

      kafkaConfig.bootstrapServers shouldBe "localhost:9092"
      kafkaConfig.topic.reactions shouldBe "reactions-topic"
      kafkaConfig.topic.mechanisms shouldBe "mechanisms-topic"
    }

    "load the HTTP configuration correctly" in {
      val httpConfig = DefaultConfigLoader.httpConfig

      httpConfig.host shouldBe Host.fromString("0.0.0.0").get
      httpConfig.port shouldBe Port.fromInt(8081).get
    }

    "load the Database configuration correctly" in {
      val databaseConfig = DefaultConfigLoader.databaseConfig

      databaseConfig.url shouldBe "jdbc:postgresql://localhost:5432/chemist_db"
      databaseConfig.user shouldBe "chemist_user"
      databaseConfig.password shouldBe "chemist_password"
    }

    "load the ChemistPreprocessorHttpClient configuration correctly" in {
      val preprocessorHttpClientConfig = DefaultConfigLoader.preprocessorHttpClientConfig

      preprocessorHttpClientConfig.baseUri shouldBe Uri.unsafeFromString("http://localhost:8080")
      preprocessorHttpClientConfig.timeout.connect shouldBe 5.seconds
      preprocessorHttpClientConfig.timeout.request shouldBe 10.seconds
      preprocessorHttpClientConfig.retries shouldBe 3
      preprocessorHttpClientConfig.pool.maxConnections shouldBe 50
      preprocessorHttpClientConfig.pool.maxIdleTime shouldBe 30.seconds
    }

    "load the ChemistEngineHttpClient configuration correctly" in {
      val engineHttpClientConfig = DefaultConfigLoader.engineHttpClientConfig

      engineHttpClientConfig.baseUri shouldBe Uri.unsafeFromString("http://localhost:8082")
      engineHttpClientConfig.timeout.connect shouldBe 5.seconds
      engineHttpClientConfig.timeout.request shouldBe 10.seconds
      engineHttpClientConfig.retries shouldBe 3
      engineHttpClientConfig.pool.maxConnections shouldBe 50
      engineHttpClientConfig.pool.maxIdleTime shouldBe 30.seconds
    }

    "load the entire AppConfig correctly" in {
      val appConfig = DefaultConfigLoader.appConfig

      appConfig.kafka.bootstrapServers shouldBe "localhost:9092"
      appConfig.http.host shouldBe Host.fromString("0.0.0.0").get
      appConfig.http.port shouldBe Port.fromInt(8081).get
      appConfig.database.url shouldBe "jdbc:postgresql://localhost:5432/chemist_db"
      appConfig.database.user shouldBe "chemist_user"
      appConfig.database.password shouldBe "chemist_password"
      appConfig.preprocessorHttpClient.baseUri shouldBe Uri.unsafeFromString("http://localhost:8080")
      appConfig.engineHttpClient.baseUri shouldBe Uri.unsafeFromString("http://localhost:8082")
    }

    "fail gracefully if required configuration is missing" in {
      intercept[ConfigReaderException[AppConfig]] {
        val invalidConfigSource = ConfigSource.string("""{ invalidKey: "invalidValue" }""")

        invalidConfigSource.loadOrThrow[AppConfig]
      }
    }
  }
}
