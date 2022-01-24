package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config
import models.HttpProtocols._
import models.UserRegistrationRequest
import service.UserRegistrationService

import scala.concurrent.ExecutionContext

class RegistrationRoutes(val conf: Config, userRegistrationService: UserRegistrationService,
                         val mat: Materializer)
                        (implicit
                         val ec: ExecutionContext) extends BaseRoutes with AuthorizationRoutes {
  val routes: Route =
    path("register") {
      (post & entity(as[UserRegistrationRequest])) { request =>
        onSuccess(userRegistrationService.register(request)) {
          case Right(response) =>
            complete(response)
          case Left(error) =>
            complete(error)
        }
      }
    }


}
