package routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import models.HttpProtocols._
import models.{CustomerDetails, UserRegistrationRequest}
import org.mockito.Mockito.when
import service.UserRegistrationService
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

class RegistrationRoutesTest extends RouteSpec with SprayJsonSupport with DefaultJsonProtocol {
  behavior of s"${this.getClass.getSimpleName}"

  val registrationService: UserRegistrationService = mock[UserRegistrationService]

  override val routes: Route = new RegistrationRoutes(conf, registrationService, materializer).routes

  val validRegistrationRequest: UserRegistrationRequest = UserRegistrationRequest(
    "test",
    "test",
    "test129@yopmail.com"
  )

  val inValidRegistrationRequest: UserRegistrationRequest = UserRegistrationRequest(
    "test",
    "test",
    "test129@yopmail.com"
  )

  val profile: CustomerDetails = CustomerDetails(
    1,
    "test",
    "test",
    "test129@yopmail.com",
    0,
    ""
  )

  implicit val CustomerDetailsFormat : RootJsonFormat[CustomerDetails] = jsonFormat6(CustomerDetails)

  val responseProfile: String =
    """
      |CustomerDetails(0, test, test, test129@yopmail.com, 0,)
      |""".stripMargin

  trait SetUpSuccess {
    when(registrationService.register(validRegistrationRequest)).thenReturn(future(Right(profile)))
  }

  trait SetUpFailure {
    when(registrationService.register(inValidRegistrationRequest)).thenReturn(future(
      Left("Error while storing registration details for email test129@yopmail.com")))
  }

  it should "execute POST /register endpoint" in new SetUpSuccess {
    Post("/register", validRegistrationRequest).signed.check {
      status shouldEqual StatusCodes.OK
      responseAs[CustomerDetails].email should not be empty
    }
  }

  it should "fail POST /register endpoint when invalid request" in new SetUpFailure {
    Post("/register").check {
      status shouldBe StatusCodes.BadRequest
    }
  }

}