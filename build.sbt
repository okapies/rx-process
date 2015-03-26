organization := "com.github.okapies"

name := "paas"

version := "1.0"

scalaVersion := "2.11.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.zaxxer"        %  "nuprocess" % "0.9.4",
  "org.scalatest"     %% "scalatest" % "2.2.4" % "test"
)
