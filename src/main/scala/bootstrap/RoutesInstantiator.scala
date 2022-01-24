package bootstrap

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import bootstrap.CORSSupport.{handleCORS, handleErrors}
import routes.{BaseRoutes, CustomerRoute, RegistrationRoutes}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class RoutesInstantiator(config: Config, services: ServiceInstantiator)(implicit
                                                                        val ec: ExecutionContext,
                                                                        val mat: Materializer,
                                                                        val logger: LoggingAdapter
) {

  private val customerRoute = new CustomerRoute(config, services.sevenDayStreakService, mat)
  private val registrartionRoute = new RegistrationRoutes(config, services.userRegistrationService, mat)

  val routes: Route = handleErrors {
    handleCORS {
      BaseRoutes.seal {
        ignoreTrailingSlash {
          BaseRoutes.logRequestResponse() {
            concat(
              customerRoute.routes,
              registrartionRoute.routes
            )
          }
        }
      }
    }
  }

}