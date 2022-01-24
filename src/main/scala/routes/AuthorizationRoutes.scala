package routes

import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpChallenges}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, extractCredentials, pass, reject}
import akka.http.scaladsl.server.{AuthenticationFailedRejection, Directive0}
import models.ErrorResponse
import models.HttpProtocols._
import com.typesafe.config.Config
import service.security.SecurityService
import spray.json._

trait AuthorizationRoutes extends BaseRoutes {

  protected val conf: Config
  private val securityConfig: Config = conf.getConfig("security")

  def authenticate: Directive0 =
    extractCredentials.flatMap {
      case Some(c: BasicHttpCredentials) =>
        authenticateRequest(c.username, c.password)
      case _ =>
        rejectUnauthenticated(AuthenticationFailedRejection.CredentialsRejected)
    }

  private def authenticateRequest(username: String,
                                  password: String): Directive0 =
    SecurityService.validateUserNameAndPassword(username, password)(
      securityConfig) match {
      case None        => pass
      case Some(error) => complete(respondWithFailure(error.toString))
    }

  private def respondWithFailure(errorMessage: String): HttpResponse = {
    val errorResponse = ErrorResponse(errorMessage, None).toJson.toString
    HttpResponse(status = StatusCodes.Unauthorized,
      entity =
        HttpEntity(ContentTypes.`application/json`, errorResponse))
  }

  private def rejectUnauthenticated(
                                     cause: AuthenticationFailedRejection.Cause): Directive0 =
    reject(
      AuthenticationFailedRejection(cause, HttpChallenges.oAuth2(""))
    )

}