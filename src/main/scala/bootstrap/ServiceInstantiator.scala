package bootstrap

import akka.actor.Scheduler
import akka.actor.typed.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.Materializer
import service.{SevenDayStreak, UserRegistrationService}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class ServiceInstantiator(conf: Config, repositoryInstantiator: RepositoryInstantiator)(implicit
                                                                                        ec: ExecutionContext,
                                                                                        log: LoggingAdapter,
                                                                                        materializer: Materializer,
                                                                                        scheduler: Scheduler
) {

  lazy val sevenDayStreakService = new SevenDayStreak(repositoryInstantiator.customerDataRepository)
  lazy val userRegistrationService = new UserRegistrationService(repositoryInstantiator.customerDataRepository)
}