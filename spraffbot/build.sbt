name := "spraffbot"

version := "1.0"

scalaVersion := "2.10.4"

retrieveManaged := true

// Normal dependencies

// Logging
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

// Akka

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.6"

libraryDependencies += "com.typesafe.akka" %% "akka-camel" % "2.3.6"

// Apache Camel IRC component
// Version of camel-irc matches the version of Apache Camel used by akka-camel above
// http://mavenbrowse.pauldoo.com/central/com/typesafe/akka/akka-camel_2.10/2.3.6/
libraryDependencies += "org.apache.camel" % "camel-irc" % "2.10.3"


// Test dependencies

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

// Optional dependencies of scalatest, version numbers match those from scalatest's pom file
// https://oss.sonatype.org/content/groups/public/org/scalatest/scalatest_2.10/2.2.1/scalatest_2.10-2.2.1.pom

libraryDependencies += "junit" % "junit" % "4.10" % "test"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.0" % "test"

