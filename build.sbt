import Dependencies._

name := "Techhub Temp"

version := "0.1"

scalaVersion := Versions.ScalaVersion

val AkkaVersion = "2.6.15"
val AkkaHttpVersion = "10.2.5"

libraryDependencies ++= Seq("org.joda" % "joda-convert" % "1.8.1",
"com.h2database" % "h2" % "1.4.196",
"mysql" % "mysql-connector-java" % "8.0.26",
"com.typesafe.slick" %% "slick" % "3.3.2",
"joda-time" % "joda-time" % "2.10.10",
"ch.qos.logback" % "logback-classic" % "1.2.3",
"com.github.tototoshi" %% "slick-joda-mapper" % "2.4.2",
"com.zaxxer" % "HikariCP" % "4.0.3",
"org.typelevel" %% "cats-core" % "2.6.1",
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "ch.megard" %% "akka-http-cors" % "1.1.2",
  "org.joda" % "joda-convert" % "1.8.1",
  "com.h2database" % "h2" % "1.4.196",
  "mysql" % "mysql-connector-java" % "8.0.26",
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "joda-time" % "joda-time" % "2.10.10",
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.2",
  "com.zaxxer" % "HikariCP" % "4.0.3",
  "org.typelevel" %% "cats-core" % "2.6.1",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
  "org.scalatestplus" %% "mockito-1-10" % "3.1.0.0" % Test,
  "org.testcontainers" % "mysql" % "1.15.3" % Test,
)

coverageExcludedPackages := "service.security.SecurityService;HttpServerApp;HttpServer;models.HttpProtocols.DateJsonFormat;bootstrap.*;routes.BaseRoutes;routes.AuthorizationRoutes"