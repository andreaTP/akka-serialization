
lazy val root = crossProject.in(file("."))
  .settings (
    name := "messages",
    organization := "eu.unicredit",
    scalaVersion := "2.11.8",
    scalacOptions := Seq("-feature", "-language:_", "-deprecation")
  )

lazy val rootJS = root.js
lazy val rootJVM = root.jvm
