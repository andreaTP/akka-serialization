import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

val akkaVersion = "2.4-SNAPSHOT"

lazy val macros = (project in file("macros")).settings(
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.reflections" % "reflections" % "0.9.10",
    "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.5.25",
    "com.lihaoyi" %% "upickle" % "0.4.1"
  )
)

val testProject = Project(
    id = "akka-serialization-tests",
    base = file(".")
  )
  .settings(SbtMultiJvm.multiJvmSettings: _*)
  .settings(PB.protobufSettings: _*)
  .settings(
    name := "akka-scalapb-tests",
    version := "2.4-SNAPSHOT",
    scalaVersion := "2.11.8",
    resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,

      //"com.lihaoyi" %% "upickle" % "0.4.1",
      "me.chrons" %% "boopickle" % "1.2.4",

      "org.scalatest" %% "scalatest" % "2.2.1" % "test"),
    // make sure that MultiJvm test are compiled by the default test compilation
    Keys.compile in MultiJvm <<= (Keys.compile in MultiJvm) triggeredBy (Keys.compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    cancelable in Global := true,
    // make sure that MultiJvm tests are executed by the default test target,
    // and combine the results from ordinary test and multi-jvm tests
    executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
      case (testResults, multiNodeResults)  =>
        val overall =
          if (testResults.overall.id < multiNodeResults.overall.id)
            multiNodeResults.overall
          else
            testResults.overall
        Tests.Output(overall,
          testResults.events ++ multiNodeResults.events,
          testResults.summaries ++ multiNodeResults.summaries)
    },
    PB.runProtoc in PB.protobufConfig := (args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))
  )
  .configs (MultiJvm)
  .dependsOn (macros)
