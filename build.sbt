organization := "com.github.okapies"

name := "rx-process"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "io.reactivex"      %  "rxjava"    % "1.0.14",
  "io.reactivex"      %% "rxscala"   % "0.25.0",
  "com.zaxxer"        %  "nuprocess" % "1.0.3",
  "org.scalatest"     %% "scalatest" % "2.2.4" % "test"
)
