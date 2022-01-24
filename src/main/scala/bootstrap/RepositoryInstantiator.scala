package bootstrap

import dao.CustomerDataRepository

trait RepositoryInstantiator {

  val customerDataRepository: CustomerDataRepository

}


