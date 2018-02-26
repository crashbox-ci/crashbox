// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released

import sbtcrossproject.{crossProject, CrossType}
import scalajscrossproject.ScalaJSCrossPlugin.autoImport.{toScalaJSGroupID => _, _}


scalaVersion in ThisBuild := "2.12.4"

lazy val shared = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    libraryDependencies += "io.crashbox" %%% "spray-json" % "2.0.0-SNAPSHOT"
  )
  .nativeSettings(
    scalaVersion := "2.11.12"
  )
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js
lazy val sharedNative = shared.native

lazy val server = (project in file("server"))
  .enablePlugins(SbtTwirl)
  .settings(
    name := "crashboxd",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.0.11",
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.h2database" % "h2" % "1.4.196"
    )
  )
  .settings(Js.dependsOnJs(ui))
  .dependsOn(sharedJvm)

lazy val ui = (project in file("ui"))
  .enablePlugins(ScalaJSPlugin)
  .disablePlugins(RevolverPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-dom_sjs0.6" % "0.9.2",
    )
  )
  .dependsOn(sharedJs)

lazy val cbx = (project in file("cbx"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    name := "cbx",
    scalaVersion := "2.11.12",
    nativeMode := "debug",
  )
  .dependsOn(sharedNative)

lazy val root = (project in file("."))
  .aggregate(server, ui, cbx)
  .settings(
    publish := {},
    publishLocal := {}
  )
