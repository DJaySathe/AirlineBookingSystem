name := """AirlineBookingSystem"""
organization := "dssathe"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.2"

libraryDependencies += guice

libraryDependencies += jdbc

libraryDependencies += javaJdbc

libraryDependencies ++= Seq(
  javaJdbc,
  javaWs,
  "org.xerial" % "sqlite-jdbc" % "3.20.0"
)
