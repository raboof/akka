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

  lazy val defaultSettings: Seq[Setting[_]] = Def.settings(
    // Makes sure that, even when compiling with a jdk version greater than 8, the resulting jar will not refer to
    // methods not found in jdk8. To test whether this has the desired effect, compile akka-remote and check the
    // invocation of 'ByteBuffer.clear()' in EnvelopeBuffer.class with 'javap -c': it should refer to
    // "java/nio/ByteBuffer.clear:()Ljava/nio/Buffer" and not "java/nio/ByteBuffer.clear:()Ljava/nio/ByteBuffer":
    scalacOptions in Compile ++= (
        // -release 8 is not enough, for some reason we need the 8 rt.jar explicitly #25330
        Seq("-release", "8")),
  )

}
