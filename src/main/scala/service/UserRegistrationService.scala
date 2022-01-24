package service

import dao.CustomerDataRepository
import models.{CustomerDetails, UserRegistrationRequest}
import routes.BaseRoutes.{IntJsonFormat, LongJsonFormat, StringJsonFormat}
import spray.json.DefaultJsonProtocol.jsonFormat6
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserRegistrationService(userDataRepository: CustomerDataRepository) {

  implicit val CustomerDetailsFormat: RootJsonFormat[CustomerDetails] = jsonFormat6(CustomerDetails)

  def register(userRegistrationRequest: UserRegistrationRequest): Future[Either[String, CustomerDetails]] = {
    userDataRepository.fetchUserDetails(userRegistrationRequest.email).flatMap {
      case Some(profile) =>
        Future.successful(Left(s"User Already Exist $profile"))
      case None =>
        createUser(userRegistrationRequest)
    }
  }

  private def createUser(userRegistrationRequest: UserRegistrationRequest): Future[Either[String, CustomerDetails]] = {
    val customerDetails = CustomerDetails(0, userRegistrationRequest.firstname, userRegistrationRequest.lastname,
      userRegistrationRequest.email, 0, "")
    userDataRepository.store(customerDetails).map {
      case true =>
        Right(customerDetails)
      case false =>
        Left(s"Error while storing registration details for email ${userRegistrationRequest.email}")
    }
  }
}
