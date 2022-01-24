package db

import com.typesafe.config.Config
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import models.CustomerDetails
import org.scalatest.BeforeAndAfterAll
import org.testcontainers.containers.MySQLContainer
import slick.jdbc.MySQLProfile.api._
import slick.util.AsyncExecutor

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CustomerDataRepositoryWithTestAPI(db: Database) extends CustomerDataRepositoryImpl(db) {

  def createTable(): Unit = {
    val schema = this.schema
    Await.result(db.run(schema.createIfNotExists), Duration.Inf)
  }

  def dropTable(): Unit = {
    val schema = this.schema
    Await.result(db.run(schema.dropIfExists), Duration.Inf)
  }

}

class CustomerDataRepositoryTest extends BaseSpec with BeforeAndAfterAll {

  behavior of s"${this.getClass.getSimpleName}"

  private val DEFAULT_CONNECTION_TIMEOUT: Int = 5000
  private val DEFAULT_QUEUE_SIZE: Int = 1000

  val dbConfig: Config = conf.getConfig("db")
  val url: String = dbConfig.getString("url")
  val user: String = dbConfig.getString("user")
  val password: String = dbConfig.getString("password")

  val dbMaxConnection: Int = dbConfig.getInt("max-connections")

  val defaultDockerImageName = s"mysql:latest"

  val container: MySQLContainer[_] = new MySQLContainer(defaultDockerImageName)

  container.withUsername(user)
  container.withPassword(password)
  container.withDatabaseName("sample")

  var db: Option[Database] = None

  override def beforeAll(): Unit = {
    container.start()

    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(container.getJdbcUrl)
    hikariConfig.setUsername(user)
    hikariConfig.setPassword(password)
    hikariConfig.setDriverClassName(dbConfig.getString("driver"))
    hikariConfig.setMaximumPoolSize(dbMaxConnection)
    hikariConfig.setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT)

    db = Some(
      Database.forDataSource(
        new HikariDataSource(hikariConfig),
        Some(dbMaxConnection),
        AsyncExecutor.apply(
          "slick-microsite-async-executor",
          dbMaxConnection,
          dbMaxConnection,
          DEFAULT_QUEUE_SIZE,
          dbMaxConnection
        )
      )
    )

    new CustomerDataRepositoryWithTestAPI(db.value).createTable()

  }

  override def afterAll(): Unit = {
    new CustomerDataRepositoryWithTestAPI(db.value).dropTable()
    container.stop()
  }

  it should "store profile and find by email" in {
    val customerDataRepositoryWithTestAPI = new CustomerDataRepositoryWithTestAPI(db.value)
    val profileEmail = s"$randomString@email.com"
    val profile = CustomerDetails(
      0,
      randomString(),
      randomString(),
      profileEmail,
      0,
      s"$randomString"
    )
    whenReady(customerDataRepositoryWithTestAPI.store(profile)) { _ =>
      val result = Await.result(customerDataRepositoryWithTestAPI.fetchUserDetails(profileEmail), Duration.Inf).value
      result.email shouldBe profile.email
      result.firstname shouldBe profile.firstname
      result.lastname shouldBe profile.lastname
    }
  }
}

