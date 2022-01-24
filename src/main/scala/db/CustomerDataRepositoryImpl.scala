package db

import dao.CustomerDataRepository
import models.CustomerDetails
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ProvenShape, Rep, TableQuery, Tag}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomerDataRepositoryImpl(db: Database) extends TableQuery(new CustomerTable(_)) with CustomerDataRepository {
  def store(customerDetails: CustomerDetails): Future[Boolean] = {
    println(s"Going to store details $customerDetails")
    db.run(this returning this.map(_.id) += customerDetails) map( _ > 0)
  }

  def update(email: String, lastLogin: String, streak: Int): Future[Boolean] = {
    db.run(this.filter(_.email === email).map(details => (details.lastLogin, details.streak))
      .update(lastLogin, streak)).map(_ > 0)
  }

  def fetchUserDetails(email: String): Future[Option[CustomerDetails]] = {
    db.run(this.filter(_.email === email).result.headOption)
  }
}

class CustomerTable(tag: Tag) extends Table[CustomerDetails](tag, "customer_details") {
  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def firstName: Rep[String] = column[String]("first_name")

  def lastName: Rep[String] = column[String]("last_name")

  def email: Rep[String] = column[String]("email")

  def streak: Rep[Int] = column[Int]("streak")

  def lastLogin: Rep[String] = column[String]("lastlogin")

  def * : ProvenShape[CustomerDetails] = (id, firstName, lastName, email, streak, lastLogin) <> (CustomerDetails.tupled, CustomerDetails.unapply)
}