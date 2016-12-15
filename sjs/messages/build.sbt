
lazy val root = crossProject.in(file("."))
  .settings (
    name := "messages-ser",
    organization := "eu.unicredit",
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-feature", "-language:_", "-deprecation"),
    libraryDependencies ++= Seq(
      "eu.unicredit" %% "serialization-macros" % "0.1-SNAPSHOT" % "provided",
      "eu.unicredit" %%% "messages" % "0.1-SNAPSHOT",

      "eu.unicredit" %%% "akkajsactor" % "0.2.4.14-SNAPSHOT",
      "eu.unicredit" %%% "akkajsactorremote" % "0.2.4.14-SNAPSHOT"

    )

  )

lazy val rootJS = root.js
lazy val rootJVM = root.jvm
