package scala

import akka.dispatch.MessageDispatcher
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{CallingThreadDispatcher, ImplicitSender, TestKit, TestKitBase}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{OptionValues, TryValues}
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future

trait CommonSpec
  extends AnyFlatSpec
    with ScalatestRouteTest
    with TestKitBase
    with ImplicitSender
    with Matchers
    with ScalaFutures
    with OptionValues
    with TryValues
    with DefaultJsonProtocol {

  implicit val logger: LoggingAdapter = system.log
  implicit lazy val timeout: Timeout = Timeout(patienceConfig.timeout)
  implicit val ec: MessageDispatcher = system.dispatchers.lookup(CallingThreadDispatcher.Id)

  protected val conf: Config = ConfigFactory.load()

  def future[A](a: A): Future[A] = Future.successful(a)

  def future[A](throwable: Throwable): Future[A] = Future.failed[A](throwable)

  protected def httpEntity(entity: String): HttpEntity.Strict =
    HttpEntity(ContentTypes.`application/json`, entity)

  override def afterAll(): Unit = {
    super.afterAll()
    TestKit.shutdownActorSystem(system, verifySystemShutdown = true)
  }

}
