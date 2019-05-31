addCommandAlias(
  name = "fixall",
  value = ";scalafixEnable;compile:scalafix;test:scalafix;test:compile;reload")

import akka.AkkaBuild._
import akka.{ AkkaBuild, Dependencies}
import sbt.Keys.{ initialCommands, parallelExecution }
import spray.boilerplate.BoilerplatePlugin

initialize := {
  // Load system properties from a file to make configuration from Jenkins easier
  loadSystemProperties("project/akka-build.properties")
  initialize.value
}

akka.AkkaBuild.buildSettings
shellPrompt := { s =>
  Project.extract(s).currentProject.id + " > "
}
resolverSettings

def isScala213: Boolean = System.getProperty("akka.build.scalaVersion", "").startsWith("2.13")

// When this is updated the set of modules in ActorSystem.allModules should also be updated
lazy val aggregatedProjects: Seq[ProjectReference] = List[ProjectReference](
    actor,
    actorTests,
    actorTestkitTyped,
    actorTyped,
    actorTypedTests,
    cluster,
    clusterMetrics,
    clusterSharding,
    clusterShardingTyped,
    clusterTools,
    clusterTyped,
    coordination,
    discovery,
    distributedData,
    docs,
    multiNodeTestkit,
    osgi,
    persistence,
    persistenceQuery,
    persistenceShared,
    persistenceTck,
    persistenceTyped,
    protobuf,
    remote,
    remoteTests,
    slf4j,
    stream,
    streamTestkit,
    streamTests,
    streamTestsTck,
    streamTyped,
    testkit) ++
  (if (isScala213) List.empty[ProjectReference]
   else
     List[ProjectReference](jackson, benchJmh, benchJmhTyped)) // FIXME #27019 remove 2.13 condition when Jackson ScalaModule has been released for Scala 2.13

lazy val root = Project(id = "akka", base = file("."))
  .aggregate(aggregatedProjects: _*)
  .settings(rootSettings: _*)

lazy val actor = akkaModule("akka-actor")
  .settings(Dependencies.actor)
  .settings(unmanagedSourceDirectories in Compile += {
    val ver = scalaVersion.value.take(4)
    (scalaSource in Compile).value.getParentFile / s"scala-$ver"
  })
  .enablePlugins(BoilerplatePlugin)

lazy val actorTests = akkaModule("akka-actor-tests")
  .dependsOn(testkit % "compile->compile;test->test")
  .settings(Dependencies.actorTests)

lazy val akkaScalaNightly = akkaModule("akka-scala-nightly")
  .aggregate(aggregatedProjects: _*)

lazy val benchJmh = akkaModule("akka-bench-jmh")
  .dependsOn(Seq(actor, stream, streamTests, persistence, distributedData, jackson, testkit).map(
    _ % "compile->compile;compile->test"): _*)
  .settings(Dependencies.benchJmh)

// typed benchmarks only on 2.12+
lazy val benchJmhTyped = akkaModule("akka-bench-jmh-typed")
  .dependsOn(Seq(persistenceTyped, distributedData, clusterTyped, testkit, benchJmh).map(
    _ % "compile->compile;compile->test"): _*)
  .settings(Dependencies.benchJmh)

lazy val cluster = akkaModule("akka-cluster")
  .dependsOn(remote, remoteTests % "test->test", testkit % "test->test")
  .settings(Dependencies.cluster)
  .settings(parallelExecution in Test := false)

lazy val clusterMetrics = akkaModule("akka-cluster-metrics")
  .dependsOn(cluster % "compile->compile;test->test", slf4j % "test->compile")
  .settings(Dependencies.clusterMetrics)
  .settings(parallelExecution in Test := false)

lazy val clusterSharding = akkaModule("akka-cluster-sharding")
// TODO akka-persistence dependency should be provided in pom.xml artifact.
//      If I only use "provided" here it works, but then we can't run tests.
//      Scope "test" is alright in the pom.xml, but would have been nicer with
//      provided.
  .dependsOn(
    cluster % "compile->compile;test->test",
    distributedData,
    persistence % "compile->compile",
    clusterTools % "compile->compile;test->test")
  .settings(Dependencies.clusterSharding)

lazy val clusterTools = akkaModule("akka-cluster-tools")
  .dependsOn(cluster % "compile->compile;test->test", coordination)
  .settings(Dependencies.clusterTools)

lazy val distributedData = akkaModule("akka-distributed-data")
  .dependsOn(cluster % "compile->compile;test->test")
  .settings(Dependencies.distributedData)

lazy val docs = akkaModule("akka-docs")
  .dependsOn(
    actor,
    cluster,
    clusterMetrics,
    slf4j,
    osgi,
    persistenceTck,
    persistenceQuery,
    distributedData,
    stream,
    actorTyped,
    clusterTools % "compile->compile;test->test",
    clusterSharding % "compile->compile;test->test",
    testkit % "compile->compile;test->test",
    remote % "compile->compile;test->test",
    persistence % "compile->compile;test->test",
    actorTyped % "compile->compile;test->test",
    persistenceTyped % "compile->compile;test->test",
    clusterTyped % "compile->compile;test->test",
    clusterShardingTyped % "compile->compile;test->test",
    actorTypedTests % "compile->compile;test->test",
    streamTestkit % "compile->compile;test->test")
  .settings(Dependencies.docs)
  .settings(
    resolvers += Resolver.jcenterRepo)

lazy val jackson = akkaModule("akka-serialization-jackson")
  .dependsOn(actor, actorTests % "test->test", testkit % "test->test")
  .settings(Dependencies.jackson)
  .settings(javacOptions += "-parameters")
  // FIXME #27019 remove when Jackson ScalaModule has been released for Scala 2.13
  .settings(crossScalaVersions -= Dependencies.scala213Version)

lazy val multiNodeTestkit = akkaModule("akka-multi-node-testkit")
  .dependsOn(remote, testkit)
  .settings(Dependencies.multiNodeTestkit)
  .settings(AkkaBuild.mayChangeSettings)

lazy val osgi = akkaModule("akka-osgi")
  .dependsOn(actor)
  .settings(Dependencies.osgi)
  .settings(parallelExecution in Test := false)

lazy val persistence = akkaModule("akka-persistence")
  .dependsOn(actor, testkit % "test->test", protobuf)
  .settings(Dependencies.persistence)
  .settings(fork in Test := true)

lazy val persistenceQuery = akkaModule("akka-persistence-query")
  .dependsOn(stream, persistence % "compile->compile;test->test", streamTestkit % "test")
  .settings(Dependencies.persistenceQuery)
  .settings(fork in Test := true)

lazy val persistenceShared = akkaModule("akka-persistence-shared")
  .dependsOn(persistence % "test->test", testkit % "test->test", remote % "test", protobuf)
  .settings(Dependencies.persistenceShared)
  .settings(fork in Test := true)

lazy val persistenceTck = akkaModule("akka-persistence-tck")
  .dependsOn(persistence % "compile->compile;test->test", testkit % "compile->compile;test->test")
  .settings(Dependencies.persistenceTck)
  .settings(fork in Test := true)

lazy val protobuf = akkaModule("akka-protobuf")

lazy val remote = akkaModule("akka-remote")
  .dependsOn(actor, stream, actorTests % "test->test", testkit % "test->test", streamTestkit % "test", protobuf)
  .settings(Dependencies.remote)
  .settings(parallelExecution in Test := false)

lazy val remoteTests = akkaModule("akka-remote-tests")
  .dependsOn(actorTests % "test->test", remote % "test->test", streamTestkit % "test", multiNodeTestkit)
  .settings(Dependencies.remoteTests)
  .settings(parallelExecution in Test := false)

lazy val slf4j = akkaModule("akka-slf4j")
  .dependsOn(actor, testkit % "test->test")
  .settings(Dependencies.slf4j)

lazy val stream = akkaModule("akka-stream")
  .dependsOn(actor, protobuf)
  .settings(Dependencies.stream)
  .enablePlugins(BoilerplatePlugin)

lazy val streamTestkit = akkaModule("akka-stream-testkit")
  .dependsOn(stream, testkit % "compile->compile;test->test")
  .settings(Dependencies.streamTestkit)

lazy val streamTests = akkaModule("akka-stream-tests")
  .dependsOn(streamTestkit % "test->test", remote % "test->test", stream)
  .settings(Dependencies.streamTests)

lazy val streamTestsTck = akkaModule("akka-stream-tests-tck")
  .dependsOn(streamTestkit % "test->test", stream)
  .settings(Dependencies.streamTestsTck)
  .settings(
    // These TCK tests are using System.gc(), which
    // is causing long GC pauses when running with G1 on
    // the CI build servers. Therefore we fork these tests
    // to run with small heap without G1.
    fork in Test := true)

lazy val testkit = akkaModule("akka-testkit")
  .dependsOn(actor)
  .settings(Dependencies.testkit)
  .settings(initialCommands += "import akka.testkit._")

lazy val actorTyped = akkaModule("akka-actor-typed")
  .dependsOn(actor)
  .settings(initialCommands :=
    """
      import akka.actor.typed._
      import akka.actor.typed.scaladsl.Behaviors
      import scala.concurrent._
      import scala.concurrent.duration._
      import akka.util.Timeout
      implicit val timeout = Timeout(5.seconds)
    """)

lazy val persistenceTyped = akkaModule("akka-persistence-typed")
  .dependsOn(
    actorTyped,
    persistence % "compile->compile;test->test",
    persistenceQuery % "test",
    actorTypedTests % "test->test",
    actorTestkitTyped % "compile->compile;test->test")
  .settings(Dependencies.persistenceShared)

lazy val clusterTyped = akkaModule("akka-cluster-typed")
  .dependsOn(
    actorTyped,
    cluster % "compile->compile;test->test",
    clusterTools,
    distributedData,
    persistence % "test->test",
    persistenceTyped % "test->test",
    protobuf,
    actorTestkitTyped % "test->test",
    actorTypedTests % "test->test",
    remoteTests % "test->test")

lazy val clusterShardingTyped = akkaModule("akka-cluster-sharding-typed")
  .dependsOn(
    clusterTyped % "compile->compile;test->test",
    persistenceTyped,
    clusterSharding,
    actorTestkitTyped % "test->test",
    actorTypedTests % "test->test",
    persistenceTyped % "test->test",
    remoteTests % "test->test")
  // To be able to import ContainerFormats.proto

lazy val streamTyped = akkaModule("akka-stream-typed")
  .dependsOn(
    actorTyped,
    stream,
    streamTestkit % "test->test",
    actorTestkitTyped % "test->test",
    actorTypedTests % "test->test")

lazy val actorTestkitTyped = akkaModule("akka-actor-testkit-typed")
  .dependsOn(actorTyped, testkit % "compile->compile;test->test")
  .settings(Dependencies.actorTestkitTyped)

lazy val actorTypedTests = akkaModule("akka-actor-typed-tests")
  .dependsOn(actorTyped, actorTestkitTyped % "compile->compile;test->test")
  .settings(AkkaBuild.mayChangeSettings)

lazy val discovery = akkaModule("akka-discovery")
  .dependsOn(actor, testkit % "test->test", actorTests % "test->test")
  .settings(Dependencies.discovery)

lazy val coordination = akkaModule("akka-coordination")
  .dependsOn(actor, testkit % "test->test", actorTests % "test->test", cluster % "test->test")
  .settings(Dependencies.coordination)

def akkaModule(name: String): Project =
  Project(id = name, base = file(name))
    .settings(akka.AkkaBuild.buildSettings)
    .settings(akka.AkkaBuild.defaultSettings)

/* Command aliases one can run locally against a module
  - where three or more tasks should be checked for faster turnaround
  - to avoid another push and CI cycle should mima or paradox fail.
  - the assumption is the user has already run tests, hence the test:compile. */
def commandValue(p: Project, externalTest: Option[Project] = None) = {
  val test = externalTest.getOrElse(p)
  val optionalMima = if (p.id.endsWith("-typed")) "" else s";${p.id}/mimaReportBinaryIssues"
  val optionalExternalTestFormat = externalTest.map(t => s";${t.id}/scalafmtAll").getOrElse("")
  s";${p.id}/scalafmtAll$optionalExternalTestFormat;${test.id}/test:compile$optionalMima;${docs.id}/paradox;${test.id}:validateCompile"
}
addCommandAlias("allActor", commandValue(actor, Some(actorTests)))
addCommandAlias("allRemote", commandValue(remote, Some(remoteTests)))
addCommandAlias("allClusterCore", commandValue(cluster))
addCommandAlias("allClusterMetrics", commandValue(clusterMetrics))
addCommandAlias("allClusterSharding", commandValue(clusterSharding))
addCommandAlias("allClusterTools", commandValue(clusterTools))
addCommandAlias(
  "allCluster",
  Seq(commandValue(cluster), commandValue(distributedData), commandValue(clusterSharding), commandValue(clusterTools)).mkString)
addCommandAlias("allCoordination", commandValue(coordination))
addCommandAlias("allDistributedData", commandValue(distributedData))
addCommandAlias("allPersistence", commandValue(persistence))
addCommandAlias("allStream", commandValue(stream, Some(streamTests)))
addCommandAlias("allDiscovery", commandValue(discovery))
addCommandAlias(
  "allTyped",
  Seq(
    commandValue(actorTyped, Some(actorTypedTests)),
    commandValue(actorTestkitTyped),
    commandValue(clusterTyped),
    commandValue(clusterShardingTyped),
    commandValue(persistenceTyped),
    commandValue(streamTyped)).mkString)
