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
    scalacOptions in Test := (scalacOptions in Test).value.filterNot(opt =>
      opt == "-Xlog-reflective-calls" || opt.contains("genjavadoc")),

    crossVersion := CrossVersion.binary,

    // Adds a `src/main/scala-2.13+` source directory for Scala 2.13 and newer
    // and a `src/main/scala-2.13-` source directory for Scala version older than 2.13
    unmanagedSourceDirectories in Compile += {
      val sourceDir = (sourceDirectory in Compile).value
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
        case _                       => sourceDir / "scala-2.13-"
      }
    },

    ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet,

    licenses := Seq(("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
    homepage := Some(url("https://akka.io/")),

    apiURL := Some(url(s"https://doc.akka.io/api/akka/${version.value}")),

    initialCommands :=
      """|import language.postfixOps
         |import akka.actor._
         |import scala.concurrent._
         |import com.typesafe.config.ConfigFactory
         |import scala.concurrent.duration._
         |import akka.util.Timeout
         |var config = ConfigFactory.parseString("akka.stdout-loglevel=INFO,akka.loglevel=DEBUG,pinned{type=PinnedDispatcher,executor=thread-pool-executor,throughput=1000}")
         |var remoteConfig = ConfigFactory.parseString("akka.remote.classic.netty{port=0,use-dispatcher-for-io=akka.actor.default-dispatcher,execution-pool-size=0},akka.actor.provider=remote").withFallback(config)
         |var system: ActorSystem = null
         |implicit def _system = system
         |def startSystem(remoting: Boolean = false) { system = ActorSystem("repl", if(remoting) remoteConfig else config); println("don’t forget to system.terminate()!") }
         |implicit def ec = system.dispatcher
         |implicit val timeout = Timeout(5 seconds)
         |""".stripMargin,

    /**
     * Test settings
     */
    fork in Test := true,

    // default JVM config for tests
    javaOptions in Test ++= {
      val defaults = Seq(
        // ## core memory settings
        "-XX:+UseG1GC",
        // most tests actually don't really use _that_ much memory (>1g usually)
        // twice used (and then some) keeps G1GC happy - very few or to no full gcs
        "-Xms3g", "-Xmx3g",
        // increase stack size (todo why?)
        "-Xss2m",

        // ## extra memory/gc tuning
        // this breaks jstat, but could avoid costly syncs to disc see http://www.evanjones.ca/jvm-mmap-pause.html
        "-XX:+PerfDisableSharedMem",
        // tell G1GC that we would be really happy if all GC pauses could be kept below this as higher would
        // likely start causing test failures in timing tests
        "-XX:MaxGCPauseMillis=300",
        // nio direct memory limit for artery/aeron (probably)
        "-XX:MaxDirectMemorySize=256m",

        // faster random source
        "-Djava.security.egd=file:/dev/./urandom")

      if (sys.props.contains("akka.ci-server"))
        defaults ++ Seq("-XX:+PrintGCTimeStamps", "-XX:+PrintGCDetails")
      else
        defaults
    },

    // all system properties passed to sbt prefixed with "akka." will be passed on to the forked jvms as is
    javaOptions in Test := {
      val base = (javaOptions in Test).value
      val akkaSysProps: Seq[String] =
        sys.props.filter(_._1.startsWith("akka"))
          .map { case (key, value) => s"-D$key=$value" }(breakOut)

      base ++ akkaSysProps
    },

    // with forked tests the working directory is set to each module's home directory
    // rather than the Akka root, some tests depend on Akka root being working dir, so reset
    testGrouping in Test := {
      val original: Seq[Tests.Group] = (testGrouping in Test).value

      original.map { group =>
        group.runPolicy match {
          case Tests.SubProcess(forkOptions) =>
            group.copy(runPolicy = Tests.SubProcess(forkOptions.withWorkingDirectory(
              workingDirectory = Some(new File(System.getProperty("user.dir"))))))
          case _ => group
        }
      }
    },

    logBuffered in Test := System.getProperty("akka.logBufferedTests", "false").toBoolean,

    // show full stack traces and test case durations
    testOptions in Test += Tests.Argument("-oDF"),

    docLintingSettings,
  )

  lazy val docLintingSettings = Seq(
  )


  def loadSystemProperties(fileName: String): Unit = {
    import scala.collection.JavaConverters._
    val file = new File(fileName)
    if (file.exists()) {
      println("Loading system properties from file `" + fileName + "`")
      val in = new InputStreamReader(new FileInputStream(file), "UTF-8")
      val props = new Properties
      props.load(in)
      in.close()
      sys.props ++ props.asScala
    }
  }

  def majorMinor(version: String): Option[String] = """\d+\.\d+""".r.findFirstIn(version)
}
