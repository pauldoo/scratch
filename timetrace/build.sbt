name := "timetrace"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.0" % "test"

// Optional dependencies of scalatest, version numbers match those from scalatest's pom file
// https://oss.sonatype.org/content/groups/public/org/scalatest/scalatest_2.10/2.1.0/scalatest_2.10-2.1.0.pom

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.0" % "test"

org.scalastyle.sbt.ScalastylePlugin.Settings
