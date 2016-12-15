enablePlugins(ScalaJSPlugin)

name := "akka.js-remote-experiments"

scalaVersion := "2.11.8"
scalacOptions := Seq("-feature", "-language:_", "-deprecation")

fork in run := true
cancelable in Global := true

libraryDependencies ++= Seq(
  "eu.unicredit" %% "serialization-macros" % "0.1-SNAPSHOT" % "provided",
  //"eu.unicredit" %%% "messages" % "0.1-SNAPSHOT",
  //"org.scala-js" %%% "scalajs-tools" % "0.6.13",

  "eu.unicredit" %%% "messages-ser" % "0.1-SNAPSHOT",

  //"eu.unicredit" %%% "akkajsactor" % "0.2.4.14-SNAPSHOT",
  //"eu.unicredit" %%% "akkajsactorremote" % "0.2.4.14-SNAPSHOT",

  "com.lihaoyi" %%% "upickle" % "0.4.1",
  "org.scala-js" %%% "scalajs-dom" % "0.9.0"
)

persistLauncher in Compile := true
scalaJSStage in Global := FastOptStage
scalaJSUseRhino in Global := false
