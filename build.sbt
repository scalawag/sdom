import de.johoop.jacoco4sbt._
import JacocoPlugin._
import com.typesafe.sbt.osgi.SbtOsgi.OsgiKeys._
import org.scalawag.sbt.gitflow.GitFlow

organization := "org.scalawag.sdom"

name := "sdom"

version := GitFlow.WorkingDir.version.toString

// When I put this at 2.10.0, the tests can't find the scala classes (ever since upgrading to sbt 0.13.0)
scalaVersion := "2.10.2"

scalacOptions ++= Seq("-unchecked","-deprecation","-feature","-target:jvm-1.6")

crossPaths := false

resolvers += "sonatype-oss-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "jaxen" % "jaxen" % "1.1.4",
  "org.scalatest" %% "scalatest" % "2.1.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

pomExtra :=
  <url>http://github.com/scalawag/sdom</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>http://github.com/scalawag/sdom.git</url>
    <connection>scm:git:git://github.com/scalawag/sdom.git</connection>
  </scm>
  <developers>
    <developer>
      <id>justinp</id>
      <name>Justin Patterson</name>
      <email>justin@scalawag.org</email>
      <url>https://github.com/justinp</url>
    </developer>
  </developers>

seq(jacoco.settings : _*)

osgiSettings

exportPackage += "org.scalawag.sdom.*"

// sdom -- Copyright 2014 Justin Patterson -- All Rights Reserved
