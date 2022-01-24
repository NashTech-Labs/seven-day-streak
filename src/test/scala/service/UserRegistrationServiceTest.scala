package service

import akka.event.LoggingAdapter
import dao.CustomerDataRepository
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.when

class UserRegistrationServiceTest extends BaseSpec {

  behavior of s"${this.getClass.getSimpleName}"

  val customerDataRepository: CustomerDataRepository = mock[CustomerDataRepository]
  override val logger: LoggingAdapter = mock[LoggingAdapter]

  val userService = new UserRegistrationService(
    customerDataRepository
  )

  val userRegistrationRequest: UserRegistrationRequest = UserRegistrationRequest(
    "firstName",
    "lastName",
    "test21@yopmail.com"
  )

  val invalidPrimaryInfo: UserRegistrationRequest = UserRegistrationRequest(
    "firstName",
    "lastName",
    ".test21@yopmail.com"
  )

  val profileEmail = s"$randomString@email.com"

  val customerDetails: CustomerDetails =
    CustomerDetails(
      0,
      "firstName",
      "lastName",
      "test21@yopmail.com",
      0,
      ""
    )

  trait RegisterSetup {
    when(customerDataRepository.fetchUserDetails(userRegistrationRequest.email)).thenReturn(future(None))
    when(customerDataRepository.store(any[CustomerDetails])).thenReturn(future(true))
  }

  trait conflictsTest {
    when(customerDataRepository.fetchUserDetails(userRegistrationRequest.email)).thenReturn(future(None))
    when(customerDataRepository.store(any[CustomerDetails])).thenReturn(future(false))

  }

  trait FailureSetup {
    when(customerDataRepository.fetchUserDetails(userRegistrationRequest.email)).thenReturn(future(None))
    when(customerDataRepository.store(customerDetails)).thenReturn(future(false))
  }

  it should "failed to create the p3 user to register due to conflicts in the request" in new conflictsTest {

    val result: Either[String, CustomerDetails] =
      userService
        .register(
          userRegistrationRequest
        )
        .futureValue
    result.isRight shouldBe false
  }

  it should "not be able to store the user details in signup event" in new FailureSetup {

    val result: Either[String, CustomerDetails] =
      userService
        .register(
          userRegistrationRequest
        )
        .futureValue
    result.isLeft shouldBe true
  }

  it should "register the new user" in {
    when(customerDataRepository.fetchUserDetails(userRegistrationRequest.email)) thenReturn future(None)
    when(customerDataRepository.store(customerDetails)) thenReturn future(true)

    val result: Either[String, CustomerDetails] =
      userService
        .register(
          userRegistrationRequest
        ).futureValue
    result.isRight shouldBe true
  }

  it should "failed due to unable to fetch by user email" in {
    when(customerDataRepository.fetchUserDetails(any[String])) thenReturn future(None)
    when(customerDataRepository.store(any[CustomerDetails])) thenReturn future(true)

    val result: Either[String, CustomerDetails] =
      userService
        .register(
          userRegistrationRequest
        ).futureValue
    result.isLeft shouldBe false
  }

  it should "failed to register as user already exists" in {
    when(customerDataRepository.fetchUserDetails(any[String])) thenReturn future(Some(customerDetails))

    val result: Either[String, CustomerDetails] =
      userService
        .register(
          userRegistrationRequest
        ).futureValue
    result.isLeft shouldBe true
  }

  it should "unable to store in user profile" in {
    when(customerDataRepository.fetchUserDetails(any[String])) thenReturn future(None)
    when(customerDataRepository.store(any[CustomerDetails])) thenReturn future(false)

    val result: Either[String, CustomerDetails] =
      userService
        .register(
          userRegistrationRequest
        ).futureValue
    result.isLeft shouldBe true
  }

}

