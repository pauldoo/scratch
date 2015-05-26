name := "timetrace"

version := "1.0"

scalaVersion := "2.11.6"

lazy val core = project

lazy val runner = project.dependsOn(core)

