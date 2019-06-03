/*
 * Copyright (C) 2016-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka

import sbt._
import Keys._

object Dependencies {

  lazy val java8CompatVersion = settingKey[String]("The version of scala-java8-compat to use.")
  val scala212Version = "2.12.8"
  val scala213Version = "2.13.0-RC2"

  val Versions = Seq(
    crossScalaVersions := Seq(scala212Version, scala213Version),
    scalaVersion := System.getProperty("akka.build.scalaVersion", crossScalaVersions.value.head),
    java8CompatVersion := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        // java8-compat is only used in a couple of places for 2.13,
        // it is probably possible to remove the dependency if needed.
        case Some((2, n)) if n >= 13 => "0.9.0"
        case _                       => "0.8.0"
      }
    },
    )

  object Compile {
    // Compile

    val config = "com.typesafe" % "config" % "1.3.4" // ApacheV2

    // For Java 8 Conversions
    val java8Compat = Def.setting { "org.scala-lang.modules" %% "scala-java8-compat" % java8CompatVersion.value } // Scala License
  }

  import Compile._
  // TODO check if `l ++=` everywhere expensive?
  val l = libraryDependencies

  val silencerVersion = "1.3.3"
  val actor = l ++= Seq(config, java8Compat.value) ++ Seq(
          compilerPlugin("com.github.ghik" %% "silencer-plugin" % silencerVersion),
          "com.github.ghik" %% "silencer-lib" % silencerVersion % "provided")

}
