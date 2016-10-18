name := "timetrace-core"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.1.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.3" % "test"

// Optional dependencies of scalatest, version numbers match those from scalatest's pom file
// https://oss.sonatype.org/content/groups/public/org/scalatest/scalatest_2.11/2.2.3/scalatest_2.11-2.2.3.pom

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"

