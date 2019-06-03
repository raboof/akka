/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka

import java.io.{FileInputStream, InputStreamReader}
import java.util.Properties
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import java.time.ZoneOffset

import sbt.Keys._
import sbt._

import scala.collection.breakOut

object AkkaBuild {
  lazy val buildSettings = Def.settings(
    Dependencies.Versions,
  )

}
