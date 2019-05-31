/*
 * Copyright (C) 2016-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka

import sbt._
import Keys._

object Dependencies {

  lazy val scalaTestVersion = settingKey[String]("The version of ScalaTest to use.")
  lazy val scalaCheckVersion = settingKey[String]("The version of ScalaCheck to use.")
  lazy val java8CompatVersion = settingKey[String]("The version of scala-java8-compat to use.")
  lazy val sslConfigVersion = settingKey[String]("The version of ssl-config to use.")
  val junitVersion = "4.12"
  val slf4jVersion = "1.7.25"
  val scalaXmlVersion = "1.0.6"
  // check agrona version when updating this
  val aeronVersion = "1.15.1"
  // needs to be inline with the aeron version
  val agronaVersion = "0.9.31"
  val nettyVersion = "3.10.6.Final"
  val jacksonVersion = "2.9.9"

  val scala212Version = "2.12.8"
  val scala213Version = "2.13.0-RC2"

  val Versions = Seq(
    crossScalaVersions := Seq(scala212Version, scala213Version),
    scalaVersion := System.getProperty("akka.build.scalaVersion", crossScalaVersions.value.head),
    scalaCheckVersion := sys.props.get("akka.build.scalaCheckVersion").getOrElse("1.14.0"),
    scalaTestVersion := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => "3.0.8-RC4"
        case _                       => "3.0.7"
      }
    },
    java8CompatVersion := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        // java8-compat is only used in a couple of places for 2.13,
        // it is probably possible to remove the dependency if needed.
        case Some((2, n)) if n >= 13 => "0.9.0"
        case _                       => "0.8.0"
      }
    },
    sslConfigVersion := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => "0.4.0"
        case _                       => "0.3.7"
      }
    })

  object Compile {
    // Compile

    val config = "com.typesafe" % "config" % "1.3.4" // ApacheV2
    val netty = "io.netty" % "netty" % nettyVersion // ApacheV2

    val scalaXml = "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion // Scala License

    val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion // MIT

    // mirrored in OSGi sample https://github.com/akka/akka-samples/tree/master/akka-sample-osgi-dining-hakkers
    val osgiCore = "org.osgi" % "org.osgi.core" % "4.3.1" // ApacheV2
    val osgiCompendium = "org.osgi" % "org.osgi.compendium" % "4.3.1" // ApacheV2

    val sigar = "org.fusesource" % "sigar" % "1.6.4" // ApacheV2

    val jctools = "org.jctools" % "jctools-core" % "2.1.2" // ApacheV2

    // reactive streams
    val reactiveStreams = "org.reactivestreams" % "reactive-streams" % "1.0.2" // CC0

    // ssl-config
    val sslConfigCore = Def.setting { "com.typesafe" %% "ssl-config-core" % sslConfigVersion.value } // ApacheV2

    val lmdb = "org.lmdbjava" % "lmdbjava" % "0.6.1" // ApacheV2, OpenLDAP Public License

    val junit = "junit" % "junit" % junitVersion // Common Public License 1.0

    // For Java 8 Conversions
    val java8Compat = Def.setting { "org.scala-lang.modules" %% "scala-java8-compat" % java8CompatVersion.value } // Scala License

    val aeronDriver = "io.aeron" % "aeron-driver" % aeronVersion // ApacheV2
    val aeronClient = "io.aeron" % "aeron-client" % aeronVersion // ApacheV2
    // Added explicitly for when artery tcp is used
    val agrona = "org.agrona" % "agrona" % agronaVersion // ApacheV2

    val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion // ApacheV2
    val jacksonAnnotations = "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion // ApacheV2
    val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion // ApacheV2
    val jacksonJdk8 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % jacksonVersion // ApacheV2
    val jacksonJsr310 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion // ApacheV2
    val jacksonScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion // ApacheV2
    val jacksonParameterNames = "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % jacksonVersion // ApacheV2
    val jacksonAfterburner = "com.fasterxml.jackson.module" % "jackson-module-afterburner" % jacksonVersion // ApacheV2
    val jacksonCbor = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % jacksonVersion // ApacheV2
    val jacksonSmile = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-smile" % jacksonVersion // ApacheV2

    object Docs {
      val sprayJson = "io.spray" %% "spray-json" % "1.3.5" % "test"
      val gson = "com.google.code.gson" % "gson" % "2.8.5" % "test"
    }

    object Test {
      val commonsMath = "org.apache.commons" % "commons-math" % "2.2" % "test" // ApacheV2
      val commonsIo = "commons-io" % "commons-io" % "2.6" % "test" // ApacheV2
      val commonsCodec = "commons-codec" % "commons-codec" % "1.11" % "test" // ApacheV2
      val junit = "junit" % "junit" % junitVersion % "test" // Common Public License 1.0
      val logback = "ch.qos.logback" % "logback-classic" % "1.2.3" % "test" // EPL 1.0 / LGPL 2.1
      val mockito = "org.mockito" % "mockito-core" % "2.19.1" % "test" // MIT
      // changing the scalatest dependency must be reflected in akka-docs/rst/dev/multi-jvm-testing.rst
      val scalatest = Def.setting { "org.scalatest" %% "scalatest" % scalaTestVersion.value % "test" } // ApacheV2
      val scalacheck = Def.setting { "org.scalacheck" %% "scalacheck" % scalaCheckVersion.value % "test" } // New BSD
      val pojosr = "com.googlecode.pojosr" % "de.kalpatec.pojosr.framework" % "0.2.1" % "test" // ApacheV2
      val tinybundles = "org.ops4j.pax.tinybundles" % "tinybundles" % "1.0.0" % "test" // ApacheV2
      val log4j = "log4j" % "log4j" % "1.2.17" % "test" // ApacheV2
      val scalaXml = "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion % "test"

      // in-memory filesystem for file related tests
      val jimfs = "com.google.jimfs" % "jimfs" % "1.1" % "test" // ApacheV2

      // docker utils
      val dockerClient = "com.spotify" % "docker-client" % "8.13.1" % "test" // ApacheV2

      // metrics, measurements, perf testing
      val metrics = "io.dropwizard.metrics" % "metrics-core" % "3.2.5" % "test" // ApacheV2
      val metricsJvm = "io.dropwizard.metrics" % "metrics-jvm" % "3.2.5" % "test" // ApacheV2
      val latencyUtils = "org.latencyutils" % "LatencyUtils" % "1.0.5" % "test" // Free BSD
      val hdrHistogram = "org.hdrhistogram" % "HdrHistogram" % "2.1.10" % "test" // CC0
      val metricsAll = Seq(metrics, metricsJvm, latencyUtils, hdrHistogram)

      // sigar logging
      val slf4jJul = "org.slf4j" % "jul-to-slf4j" % slf4jVersion % "test" // MIT
      val slf4jLog4j = "org.slf4j" % "log4j-over-slf4j" % slf4jVersion % "test" // MIT

      // reactive streams tck
      val reactiveStreamsTck = "org.reactivestreams" % "reactive-streams-tck" % "1.0.2" % "test" // CC0
    }

    object Provided {
      // TODO remove from "test" config
      // If changed, update akka-docs/build.sbt as well
      val sigarLoader = "io.kamon" % "sigar-loader" % "1.6.6-rev002" % "optional;provided;test" // ApacheV2

      val activation = "com.sun.activation" % "javax.activation" % "1.2.0" % "provided;test"

      val levelDB = "org.iq80.leveldb" % "leveldb" % "0.10" % "optional;provided" // ApacheV2
      val levelDBmultiJVM = "org.iq80.leveldb" % "leveldb" % "0.10" % "optional;provided" // ApacheV2
      val levelDBNative = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8" % "optional;provided" // New BSD

      val junit = Compile.junit % "optional;provided;test"

      val scalatest = Def.setting { "org.scalatest" %% "scalatest" % scalaTestVersion.value % "optional;provided;test" } // ApacheV2

    }

  }

  import Compile._
  // TODO check if `l ++=` everywhere expensive?
  val l = libraryDependencies

  val silencerVersion = "1.3.3"
  val actor = l ++= Seq(config, java8Compat.value) ++ Seq(
          compilerPlugin("com.github.ghik" %% "silencer-plugin" % silencerVersion),
          "com.github.ghik" %% "silencer-lib" % silencerVersion % "provided")

}
