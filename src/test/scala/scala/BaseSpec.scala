package scala

import akka.http.scaladsl.HttpExt
import org.scalatestplus.mockito.MockitoSugar

import scala.util.Random

trait BaseSpec extends CommonSpec with MockitoSugar {

  protected val httpMock: HttpExt = mock[HttpExt]

  protected def randomString(length: Int): String =
    Random.alphanumeric.take(length).mkString

  protected def randomString(): String = randomString(10)

  protected def randomBoolean(): Boolean = Random.nextBoolean()

  protected def randomPath(): String =
    List
      .fill(Random.nextInt(5)) {
        randomString()
      }
      .mkString("/")

  protected def randomPath(extension: String): String = randomPath() + "." + extension

  protected def randomInt(max: Int): Int = Random.nextInt(max)

  protected def randomInt(min: Int, max: Int): Int = Random.nextInt(max - min) + min
}
