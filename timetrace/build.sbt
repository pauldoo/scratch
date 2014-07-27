name := "timetrace"

version := "1.0"

scalaVersion := "2.11.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"

// Optional dependencies of scalatest, version numbers match those from scalatest's pom file
// http://mavenbrowse.pauldoo.com/central/org/scalatest/scalatest_2.11/2.2.0/

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"

org.scalastyle.sbt.ScalastylePlugin.Settings
