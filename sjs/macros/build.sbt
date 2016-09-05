
name := "serialization-macros"

organization := "eu.unicredit"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.reflections" % "reflections" % "0.9.10",
    "com.lihaoyi" %% "upickle" % "0.4.1"
)
