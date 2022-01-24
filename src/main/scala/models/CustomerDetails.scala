package models

final case class CustomerDetails(id: Long, firstname: String, lastname: String, email: String,
                                 streak: Int, lastLogin: String)

final case class UserProfile (email: String)

final case class UserRegistrationRequest(firstname: String, lastname: String, email: String)

final case class UpdatedInfo(streaks: Int, login: String)