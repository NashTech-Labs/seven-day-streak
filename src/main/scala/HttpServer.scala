import akka.actor.typed.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import command.Command

class HttpServer(httpConfig: Config)(implicit
                                     system: ActorSystem[Command],
                                     ec: ExecutionContext,
                                     materializer: Materializer,
                                     logger: LoggingAdapter
) {

  def start(routes: Route): Future[Http.ServerBinding] = {
    val interface = httpConfig.getString("interface")
    val port = httpConfig.getInt("port")
    val micrositePrefix = httpConfig.getString("microsite")

    val prefixedRoutes = pathPrefix(micrositePrefix).apply(routes)

    val bindingFuture: Future[Http.ServerBinding] = Http().newServerAt(interface, port).bindFlow(prefixedRoutes)

    bindingFuture.andThen {
      case Success(_) =>
        logger.info("Has bound server on {}:{}", interface, port)
      case Failure(cause) =>
        logger.error(cause, "Has failed to bind to {}:{}", interface, port)
    }

  }

}

