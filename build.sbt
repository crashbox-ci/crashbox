scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka"            %% "akka-http-core"                 % "10.0.11",
  "com.typesafe.akka"            %% "akka-http-spray-json"           % "10.0.11",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.4.196",
  "xyz.driver" %% "spray-json-derivation" % "0.1.1"
)

enablePlugins(SbtTwirl)