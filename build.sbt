import sbt.Keys._

ThisBuild / organization := "io.constellationnetwork"
ThisBuild / organizationName := "Constellation Network"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / crossScalaVersions := Seq("2.12.19", "2.13.16")
ThisBuild / version := sys.env.get("RELEASE_TAG").map(_.stripPrefix("v")).getOrElse("0.1.0-SNAPSHOT")

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

ThisBuild / sonatypeCredentialHost := "central.sonatype.com"
ThisBuild / sonatypeRepository := "https://central.sonatype.com/api/v1/publisher"
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / publishMavenStyle := true
ThisBuild / versionScheme := Some("early-semver")

// PGP settings for signing
ThisBuild / credentials += Credentials(
  "GnuPG Key ID",
  "gpg",
  sys.env.getOrElse("PGP_KEY_ID", ""),
  "ignored"
)

lazy val root = (project in file("."))
  .disablePlugins(ScalafixPlugin)
  .settings(
    name := "constellation-scalafix-rules",
    libraryDependencies ++= Seq(
      "ch.epfl.scala" %% "scalafix-core" % _root_.scalafix.sbt.BuildInfo.scalafixVersion
    ),
    scalacOptions ++= Seq(
      "-Yrangepos",
      "-language:higherKinds",
      "-language:existentials",
      "-deprecation",
      "-feature"
    )
  )
