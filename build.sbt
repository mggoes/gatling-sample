enablePlugins(GatlingPlugin)

scalaVersion := "2.13.3"
name := "gatling-sample"
organization := "br.com.sample"
version := "1.0"

val gatlingVersion = "3.5.1"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % "test,it"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion % "test,it"
