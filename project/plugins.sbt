addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.3.8")

libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "3.0.0-b2",
  "com.trueaccord.scalapb" %% "compilerplugin" % "0.5.21"
)

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.25")
