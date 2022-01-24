package service

import dao.CustomerDataRepository
import models.HttpProtocols.{IntJsonFormat, LongJsonFormat, StringJsonFormat}
import models.{CustomerDetails, UpdatedInfo, UserProfile}
import org.joda.time.DateTime
import spray.json.DefaultJsonProtocol.{jsonFormat2, jsonFormat6}
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SevenDayStreak(customerDataRepository: CustomerDataRepository) {
  val NumberZero = 0
  val SuccessKey = "success"

  implicit val CustomerDetailsFormat: RootJsonFormat[CustomerDetails] = jsonFormat6(CustomerDetails)
  implicit val UpdatedInfoFormat: RootJsonFormat[UpdatedInfo] = jsonFormat2(UpdatedInfo)

  def login(userProfile: UserProfile): Future[UserProfile] = {
    sevenDayStreak(userProfile)

    Future.successful(userProfile)
  }

  private def sevenDayStreak(login: UserProfile): Future[Future[String]] = {
    customerDataRepository.fetchUserDetails(login.email).map {
      case Some(customerDetails: CustomerDetails) =>

        if (customerDetails.lastLogin.contains("")) {
          startSevenDayStreak(customerDetails)
        }
        else {
          proceedSevenDayStreak(customerDetails)
        }
      case None =>
        Future.successful("errorMessage")
    }
  }

  def isDailyLogin(lastLogin: String): Boolean = {

    val currentDate = new DateTime()
    val currentDateInMillis = currentDate.getMillis
    val lastLoginDate = DateTime.parse(lastLogin)
    val lastLoginInMillis = lastLoginDate.getMillis
    val timeDifferenceOfLogin = (currentDateInMillis - lastLoginInMillis).toInt
    val days = timeDifferenceOfLogin / (60 * 60 * 24 * 1000)
    if (days == 1) {
      if (currentDate.getDayOfYear - lastLoginDate.getDayOfYear == 1) true else false
    }
    else if (days < 1) {
      if (currentDate.getDayOfYear == lastLoginDate.getDayOfYear) false else true
    }
    else {
      false
    }
  }

  private def startSevenDayStreak(platformUser: CustomerDetails): Future[String] = {
    val time = new DateTime()
    val streak = 1
    val last_login = time.getMillis.toString

    customerDataRepository.update(platformUser.email, last_login, streak)

    Future.successful("seven day streak started")
  }

  private def proceedSevenDayStreak(platformUser: CustomerDetails): Future[String] = {
    val streak = platformUser.streak
    val lastLogin = platformUser.lastLogin
    streak match {
      case 7 =>
        Future.successful("Reward Already Redeemed")
      case 6 if isDailyLogin(lastLogin) =>
        Future.successful("Streak Completed")
      case _: Int =>
        updateSevenDayStreakProgress(platformUser)
    }
  }

  private def updateSevenDayStreakProgress(platformUser: CustomerDetails): Future[String] = {
    val time = new DateTime()
    val lastLogin = platformUser.lastLogin
    val streak = platformUser.streak

    val updatedInfo = if (isDailyLogin(lastLogin)) {
      val streaks = streak + 1
      val last = time.toString
      UpdatedInfo(streaks, last)
    }
    else if (isSameDayLogin(lastLogin)) {
      val streaks = streak
      val last = time.toString
      UpdatedInfo(streaks, last)

    }
    else {
      val streaks = 1
      val last = time.toString
      UpdatedInfo(streaks, last)
    }
    customerDataRepository.update(platformUser.email, updatedInfo.login, updatedInfo.streaks)
    Future.successful("Updated streak and last login")
  }

  def isSameDayLogin(lastLogin: String): Boolean = {
    val currentDate = new DateTime()
    val currentDateInMillis = currentDate.getMillis
    val lastLoginDate = DateTime.parse(lastLogin)
    val lastLoginInMillis = lastLoginDate.getMillis
    val timeDifferenceOfLogin = (currentDateInMillis - lastLoginInMillis).toInt
    val days = timeDifferenceOfLogin / (60 * 60 * 24 * 1000)
    if (days == 0) {
      if (currentDate.getDayOfYear - lastLoginDate.getDayOfYear == 1) false else true
    }
    else
      false
  }
}
