scalaVersion in ThisBuild := "2.12.4"

def shared = Seq(
  unmanagedSourceDirectories in Compile += (baseDirectory in ThisBuild).value / "shared" / "src" / "main" / "scala",
  libraryDependencies += "com.propensive" %%% "magnolia" % "0.7.0"
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
    )
  )
  .settings(Js.dependsOnJs(ui))
  .settings(shared)

lazy val ui = (project in file("ui"))
  .enablePlugins(ScalaJSPlugin)
  .disablePlugins(RevolverPlugin)
  .settings(shared)
  .settings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"
  )

lazy val root = (project in file("."))
  .aggregate(server, ui)
  .settings(
    publish := {},
    publishLocal := {}
  )
