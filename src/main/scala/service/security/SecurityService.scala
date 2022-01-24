package service.security

import SecurityServiceError.InvalidTokenProvided
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol

sealed trait SecurityServiceError

object SecurityServiceError {
  case object InvalidTokenProvided extends SecurityServiceError
}

object SecurityService extends DefaultJsonProtocol {

  def validateUserNameAndPassword(userName: String, password: String)(
    securityConfig: Config
  ): Option[SecurityServiceError] = {
    val clientId: String = securityConfig.getString("client-id")
    val secretKey: String = securityConfig.getString("secret-key")
    if (userName == clientId && password == secretKey) None
    else Some(InvalidTokenProvided)
  }

}