import akka.{Done, actor}
import akka.actor.CoordinatedShutdown
import akka.actor.CoordinatedShutdown.Reason
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, Scheduler, Terminated}
import akka.event.{Logging, LoggingAdapter}
import akka.stream.Materializer
import bootstrap.{RoutesInstantiator, ServiceInstantiator, SqlRepositoryInstantiator}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import command._

object HttpServerApp extends App {

  def apply(): Behavior[Command] =
    Behaviors.setup { _ =>
      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          println("terminated")
          Behaviors.stopped
      }
    }

  implicit val system: ActorSystem[Command] = ActorSystem[Command](HttpServerApp(), "http-server-app")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
  implicit val materializer: Materializer = Materializer(system)
  implicit val logger: LoggingAdapter = Logging(system.classicSystem, "Seven-day-streak")
  implicit val scheduler: actor.Scheduler = system.classicSystem.scheduler

  val conf = ConfigFactory.load()

  val akkaShutdown = CoordinatedShutdown(system)

  val repositoryInstantiator = new SqlRepositoryInstantiator(conf.getConfig("db"))

  val serviceInstantiator = new ServiceInstantiator(conf, repositoryInstantiator)

  val routesInstantiator = new RoutesInstantiator(conf, serviceInstantiator)

  val routes = routesInstantiator.routes

  private val httpServerConfig = conf.getConfig("http")

  val httpServer = new HttpServer(httpServerConfig)

  val serverBinding = httpServer.start(routes).andThen {
    case Success(_) =>
      println("""
                |Knoldus Inc
                |""".stripMargin)
    case Failure(exception) => shutdown(exception)
  }

  akkaShutdown.addTask(CoordinatedShutdown.PhaseServiceUnbind, "Unbinding HTTP Server") { () =>
    serverBinding.transformWith {
      case Success(binding) =>
        binding.unbind().andThen {
          case Success(_) => logger.info("Has unbounded http server")
          case Failure(exception) => logger.error(exception, "Has failed to unbind http server")
        }
    }
  }

  private def shutdown(e: Throwable): Future[Done] = {
    logger.error(e, "Error starting application:")
    akkaShutdown.run(new Reason {
      override def toString: String = "Error starting application: " ++ e.getMessage
    })
  }

}

