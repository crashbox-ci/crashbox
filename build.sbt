scalaVersion in ThisBuild := "2.12.4"

def shared = Seq(
  unmanagedSourceDirectories in Compile += (baseDirectory in ThisBuild).value / "shared" / "src" / "main" / "scala"
)

lazy val server = (project in file("server"))
  .enablePlugins(SbtTwirl)
  .settings(
    name := "crashboxd",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.0.11",
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.h2database" % "h2" % "1.4.196",
      "io.crashbox" %% "spray-json" % "2.0.0-SNAPSHOT"
    )
  )
  .settings(Js.dependsOnJs(ui))
  .settings(shared)

lazy val ui = (project in file("ui"))
  .enablePlugins(ScalaJSPlugin)
  .disablePlugins(RevolverPlugin)
  .settings(shared)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-dom_sjs0.6" % "0.9.2",
      "io.crashbox" %% "spray-json_sjs0.6" % "2.0.0-SNAPSHOT"
    )
  )

lazy val cbx = (project in file("cbx"))
  .enablePlugins(ScalaNativePlugin)
  .settings(
    name := "cbx",
    scalaVersion := "2.11.12",
    nativeMode := "debug",
    libraryDependencies ++= Seq(
      "io.crashbox" %% "spray-json_native0.3" % "2.0.0-SNAPSHOT"
    )
  )
.settings(shared)


lazy val root = (project in file("."))
  .aggregate(server, ui, cbx)
  .settings(
    publish := {},
    publishLocal := {}
  )
