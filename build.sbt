import sbt.Keys._

ThisBuild / organization := "io.constellationnetwork"
ThisBuild / organizationName := "Constellation Network"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / crossScalaVersions := Seq("2.12.19", "2.13.16")
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / homepage := Some(url("https://github.com/Constellation-Labs/constellation-scalafix-rules"))
ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / developers := List(
  Developer(
    "constellation-contributors",
    "Constellation Contributors",
    "contact@constellationnetwork.io",
    url("https://github.com/Constellation-Labs/constellation-scalafix-rules/graphs/contributors")
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "constellation-scalafix-rules",
    libraryDependencies ++= Seq(
      "ch.epfl.scala" %% "scalafix-core" % "0.14.3"
    ),
    scalacOptions ++= Seq(
      "-Yrangepos",
      "-language:higherKinds",
      "-language:existentials",
      "-deprecation",
      "-feature"
    )
  )
