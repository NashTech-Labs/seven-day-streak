package routes

import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, HttpCredentials}
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpRequest, Uri}
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol

trait RouteSpec extends BaseSpec with DefaultJsonProtocol { self =>

  val routes: Route

  val clientId: String = conf.getString("security.client-id")
  val secretKey: String = conf.getString("security.secret-key")

  val credentials: HttpCredentials = BasicHttpCredentials(clientId, secretKey)

  implicit class ExtendedHttpRequest(request: HttpRequest) {

    def withQuery(query: Uri.Query): HttpRequest =
      request.copy(uri = request.uri.withQuery(query))

    def withQuery(query: Map[String, String]): HttpRequest =
      withQuery(Uri.Query(query))

    def withQuery(query: (String, String)*): HttpRequest =
      withQuery(query.toMap)

    def withCredentials(credentials: HttpCredentials): HttpRequest =
      request ~> addCredentials(credentials)

    def signed: HttpRequest =
      request.addHeader(
        Authorization(credentials)
      )

    def check[T](body: => T): T =
      check(ContentTypes.`application/json`)(body)

    def check[T](expectedContentType: ContentType)(body: => T): T =
      request ~> handledRoutes ~> self.check {
        contentType shouldBe expectedContentType
        body
      }

    def run(): RouteTestResult =
      request ~> handledRoutes ~> runRoute

    def runSeal(): RouteTestResult =
      request ~> Route.seal(handledRoutes) ~> runRoute

    private val handledRoutes = BaseRoutes.seal(routes)

  }

}

