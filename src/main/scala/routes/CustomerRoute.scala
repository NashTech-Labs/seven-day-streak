package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config
import models.HttpProtocols._
import models.UserProfile
import service.SevenDayStreak

import scala.concurrent.ExecutionContext

class CustomerRoute(val conf: Config,
                    sevenDayStreak: SevenDayStreak,
                    val mat: Materializer)(implicit
                                           val ec: ExecutionContext
                   ) extends BaseRoutes with AuthorizationRoutes {

  val routes: Route =
    pathPrefix("login") {
      path("user") {
          (post & entity(as[UserProfile])) { request: UserProfile =>
            onSuccess(sevenDayStreak.login(request)) {
              case response =>
                complete(response)
              case error =>
                complete(error)
            }
          }
        }
      }
    }
