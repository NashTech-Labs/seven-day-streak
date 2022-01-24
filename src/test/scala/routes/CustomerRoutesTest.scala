package routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import models.UserProfile
import org.mockito.Mockito.when
import service.SevenDayStreak
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

class CustomerRoutesTest extends RouteSpec with SprayJsonSupport with DefaultJsonProtocol {
  behavior of s"${this.getClass.getSimpleName}"

  val customerService: SevenDayStreak = mock[SevenDayStreak]

  override val routes: Route = new CustomerRoute(conf, customerService, materializer).routes
  val emailOriginal: String = randomString() + "@abc.xyz"
  val emailNew: String = randomString() + "@xyz@abc"

  val validCustomerRequest: UserProfile = UserProfile(
    "test129@yopmail.com"
  )

  val inValidCustomerRequest: UserProfile = UserProfile(
    ""
  )

  val profile: UserProfile = UserProfile(
    "test129@yopmail.com"
  )

  implicit val UserProfileFormat: RootJsonFormat[UserProfile] = jsonFormat1(UserProfile)

  val responseProfile: String =
    """
      |UserProfile(test129@yopmail.com)
      |""".stripMargin

  trait SetUpSuccess {
    when(customerService.login(validCustomerRequest)).thenReturn(future(profile))
  }

  trait SetUpFailure {
    when(customerService.login(inValidCustomerRequest)).thenReturn(future(profile))
  }

  it should "execute POST /login/user endpoint" in new SetUpSuccess {
    Post("/login/user", validCustomerRequest).signed.check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "fail POST /login/user endpoint when invalid request" in new SetUpFailure {
    Post("/login/user").check {
      status shouldBe StatusCodes.BadRequest
    }
  }

}