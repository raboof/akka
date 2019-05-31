import akka.AkkaBuild._
import akka.{ AkkaBuild, Dependencies}

lazy val aggregatedProjects: Seq[ProjectReference] = List[ProjectReference](
    actor,
    )

lazy val root = Project(id = "akka", base = file("."))
  .aggregate(aggregatedProjects: _*)

lazy val actor = akkaModule("akka-actor")
  .settings(Dependencies.actor)

def akkaModule(name: String): Project =
  Project(id = name, base = file(name))
    .settings(akka.AkkaBuild.buildSettings)
    .settings(akka.AkkaBuild.defaultSettings)

