import akka.{ AkkaBuild, Dependencies }

lazy val root = Project(id = "akka", base = file("akka-actor"))
  .settings(Dependencies.actor)
  .settings(akka.AkkaBuild.buildSettings)
  .settings(akka.AkkaBuild.defaultSettings)

