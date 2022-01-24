package service

import akka.event.LoggingAdapter
import dao.CustomerDataRepository
import models._
import org.joda.time.DateTime
import org.mockito.Matchers.any
import org.mockito.Mockito.when

class SevenDayStreakTest extends BaseSpec {

  behavior of s"${this.getClass.getSimpleName}"

  val customerDataRepository: CustomerDataRepository = mock[CustomerDataRepository]
  override val logger: LoggingAdapter = mock[LoggingAdapter]

  val sevenDayStreak = new SevenDayStreak(
    customerDataRepository
  )

  val userProfile: UserProfile = UserProfile(
    "test21@yopmail.com"
  )

  val noUserProfile: UserProfile = UserProfile(
    "tes3@gmail.com"
  )

  val invalidUserProfile: UserProfile = UserProfile(
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

  val customerDetailsStreakCompleted: CustomerDetails =
    CustomerDetails(
      0,
      "firstName",
      "lastName",
      "test21@yopmail.com",
      7,
      new DateTime().getMillis.toString
    )

  val customerDetailsProceedStreak: CustomerDetails =
    CustomerDetails(
      0,
      "firstName",
      "lastName",
      "test21@yopmail.com",
      2,
      new DateTime().getMillis.toString
    )

  val customerDetailsLastStreak: CustomerDetails =
    CustomerDetails(
      0,
      "firstName",
      "lastName",
      "test21@yopmail.com",
      6,
      new DateTime().getMillis.toString
    )

  trait RegisterSetup {
    when(customerDataRepository.fetchUserDetails(userProfile.email)).thenReturn(future(None))
    when(customerDataRepository.store(any[CustomerDetails])).thenReturn(future(true))
  }

  trait conflictsTest {
    when(customerDataRepository.fetchUserDetails(userProfile.email)).thenReturn(future(None))
    when(customerDataRepository.store(any[CustomerDetails])).thenReturn(future(false))

  }

  trait FailureSetup {
    when(customerDataRepository.fetchUserDetails(userProfile.email)).thenReturn(future(None))
    when(customerDataRepository.store(customerDetails)).thenReturn(future(false))
  }

  it should "start streak as first login by the user" in {
    when(customerDataRepository.fetchUserDetails(userProfile.email)) thenReturn future(Some(customerDetails))
    when(customerDataRepository.update(userProfile.email, new DateTime().getMillis.toString, 1)) thenReturn future(true)

    val result: UserProfile =
      sevenDayStreak
        .login(
          userProfile
        ).futureValue
    result shouldBe userProfile
  }

  it should "not be able to fetch user details" in {
    when(customerDataRepository.fetchUserDetails(noUserProfile.email)) thenReturn future(None)

    val result =
      sevenDayStreak
        .login(
          noUserProfile
        ).futureValue
    result shouldBe UserProfile("tes3@gmail.com")
  }

  it should "streak completed and reward received by the user" in {
    when(customerDataRepository.fetchUserDetails(userProfile.email)) thenReturn future(Some(customerDetailsStreakCompleted))

    val result: UserProfile =
      sevenDayStreak
        .login(
          userProfile
        ).futureValue
    result shouldBe userProfile
  }

  it should "show last streak completed" in {
    when(customerDataRepository.fetchUserDetails(userProfile.email)) thenReturn future(Some(customerDetailsLastStreak))

    val result: UserProfile =
      sevenDayStreak
        .login(
          userProfile
        ).futureValue
    result shouldBe userProfile
  }

  it should "proceed with streak as not first login by the user" in {
    when(customerDataRepository.fetchUserDetails(userProfile.email)) thenReturn future(Some(customerDetailsProceedStreak))
    when(customerDataRepository.update(userProfile.email, new DateTime().getMillis.toString, 3)) thenReturn future(true)
    val result: UserProfile =
      sevenDayStreak
        .login(
          userProfile
        ).futureValue
    result shouldBe userProfile
  }
}

