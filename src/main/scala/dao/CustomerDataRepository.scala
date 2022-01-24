package dao

import models.CustomerDetails

import scala.concurrent.Future

trait CustomerDataRepository {
  def store(customerDetails: CustomerDetails): Future[Boolean]

  def update(email: String, lastLogin: String, streak: Int): Future[Boolean]

  def fetchUserDetails(email: String): Future[Option[CustomerDetails]]
}
