/*
 * Copyright (C) 2016-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka

import sbt._
import Keys._

object Dependencies {

  lazy val java8CompatVersion = settingKey[String]("The version of scala-java8-compat to use.")

  object Compile {
    // Compile

    val config = "com.typesafe" % "config" % "1.3.4" // ApacheV2

    // For Java 8 Conversions
    val java8Compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
  }

  import Compile._
  // TODO check if `l ++=` everywhere expensive?
  val l = libraryDependencies

  val silencerVersion = "1.3.3"
  val actor = l ++= Seq(config, java8Compat) ++ Seq(
          compilerPlugin("com.github.ghik" %% "silencer-plugin" % silencerVersion),
          "com.github.ghik" %% "silencer-lib" % silencerVersion % "provided")

}
