package bootstrap

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{corsRejectionHandler, _}
import ch.megard.akka.http.cors.scaladsl.model.{HttpHeaderRange, HttpOriginMatcher}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

object CORSSupport {
  val rejectionHandler = corsRejectionHandler.withFallback(RejectionHandler.default)

  val exceptionHandler = ExceptionHandler {
    case e: NoSuchElementException => complete(StatusCodes.NotFound -> e.getMessage)
  }
  val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

  private val corsSettings = CorsSettings.defaultSettings
    .withAllowedMethods(scala.collection.immutable.Seq(GET, POST, PUT, HEAD, DELETE, OPTIONS))
    .withAllowedOrigins(HttpOriginMatcher.*)
    .withAllowedHeaders(HttpHeaderRange.*)
    .withAllowCredentials(false)
    .withAllowGenericHttpRequests(true)

  def handleCORS(routes: Route): Route =
    cors(corsSettings) {
      routes
    }
}