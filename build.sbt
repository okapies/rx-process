organization := "com.github.okapies"

name := "rx-process"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "io.reactivex"      %  "rxjava"    % "1.0.9",
  "io.reactivex"      %% "rxscala"   % "0.24.1",
  "com.zaxxer"        %  "nuprocess" % "0.9.5",
  "org.scalatest"     %% "scalatest" % "2.2.4" % "test"
)
