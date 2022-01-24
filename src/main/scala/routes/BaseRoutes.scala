package routes

import akka.event.Logging.LogLevel
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.ContentType.NonBinary
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import akka.stream.Materializer
import models.ErrorResponse
import models.HttpProtocols.ErrorResponseFormat
import org.joda.time.DateTime
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.Try

trait BaseRoutes {

  implicit val ec: ExecutionContext
  implicit val mat: Materializer

  protected def Ok[T](response: T)(implicit marshaller: ToEntityMarshaller[T]): StandardRoute =
    complete(StatusCodes.OK, response)

  protected def segment[T](f: PartialFunction[String, T]): PathMatcher1[T] =
    PathMatchers.Segment.flatMap(f.lift(_))

}

object BaseRoutes extends DefaultJsonProtocol {

  def seal(route: Route)(implicit logger: LoggingAdapter): Route = {

    val exceptionHandler = ExceptionHandler {
      case e: Exception =>
        logger.error(e, s"Unhandled exception caught: ${e.getMessage}")
        complete(
          HttpResponse(
            StatusCodes.InternalServerError,
            Nil,
            HttpEntity(
              ContentTypes.`application/json`,
              ErrorResponse(
                StatusCodes.InternalServerError.reason,
                Some(e.getMessage)
              ).toJson.toString()
            )
          )
        )
    }

    val rejectionHandler = RejectionHandler
      .newBuilder()
      .handle {
        case ValidationRejection(msg, _) =>
          complete((StatusCodes.BadRequest, "Invalid entry: " + msg))
      }
      .result()
      .withFallback(RejectionHandler.default)
      .mapRejectionResponse {
        // since all Akka default rejection responses are Strict this will handle all other rejections
        case res @ HttpResponse(_, _, _: HttpEntity.Strict, _) =>
          res.withEntity(
            entity = HttpEntity(
              ContentTypes.`application/json`,
              ""
            )
          )
        case x => x // pass through all other types of responses
      }

    (handleExceptions(exceptionHandler) & handleRejections(rejectionHandler))(route)
  }

  def logRequestResponse(logLevel: LogLevel = Logging.InfoLevel): Directive0 =
    DebuggingDirectives.logRequestResult(
      LoggingMagnet((loggingAdapter: LoggingAdapter) => logRequestResponse(loggingAdapter, logLevel))
    )

  private def logRequestResponse(log: LoggingAdapter, logLevel: LogLevel): HttpRequest => RouteResult => Unit = {
    val requestTime = DateTime.now
    requestResponseTimeLogging(log, requestTime, logLevel)
  }

  private def requestResponseTimeLogging(
                                          loggingAdapter: LoggingAdapter,
                                          requestTime: DateTime,
                                          level: LogLevel
                                        )(httpRequest: HttpRequest)(routeResult: RouteResult): Unit = {
    val responseTime = DateTime.now()
    val requestInformation = getRequestAsString(httpRequest)
    val logEntry = routeResult match {
      case Complete(httpResponse) =>
        val responseAsString = getResponseAsString(httpResponse)
        val loggingString = s"Request [$requestTime]: $requestInformation \nResponse [$responseTime]: $responseAsString"
        LogEntry(loggingString, level)

      case Rejected(rejection) =>
        val rejectionString = rejection.mkString(",")
        val loggingString =
          s"Request [$requestTime]: $requestInformation \nRejected Response [$responseTime]: $rejectionString"
        LogEntry(loggingString, level)
    }
    logEntry.logTo(loggingAdapter)
  }

  private def getEntityAsString(httpEntity: HttpEntity, maxBytes: Int = 2048) =
    httpEntity match {
      case HttpEntity.Strict(contentType: NonBinary, data) =>
        Try {
          if (data.length > maxBytes) {
            val truncatedString = data.take(maxBytes).decodeString(contentType.charset.value)
            s"$truncatedString ... (${data.length} bytes total)"
          } else
            data.decodeString(contentType.charset.value)
        }.getOrElse(data.toString())

      case httpEntity => httpEntity.toString
    }

  private def getResponseAsString(httpResponse: HttpResponse): String =
    httpResponse match {
      case HttpResponse(status, _, entity, _) =>
        val entityAsString = getEntityAsString(entity)
        s"HttpResponse: $status, $entityAsString"
    }

  private def getRequestAsString(httpRequest: HttpRequest): String =
    httpRequest match {
      case HttpRequest(method, uri, _, entity, _) =>
        val entityAsString = getEntityAsString(entity)
        s"HttpRequest: [${method.name()}] - $uri, $entityAsString"
    }

}