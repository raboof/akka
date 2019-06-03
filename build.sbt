import akka.{ AkkaBuild, Dependencies }

scalacOptions in Compile ++= (
    // -release 8 is not enough, for some reason we need the 8 rt.jar explicitly #25330
    Seq("-release", "8"))

lazy val root = Project(id = "akka", base = file("akka-actor"))
  .settings(Dependencies.actor)
  .settings(akka.AkkaBuild.buildSettings)
  .settings(
scalacOptions in Compile ++= (
    // -release 8 is not enough, for some reason we need the 8 rt.jar explicitly #25330
    Seq("-release", "8"))
)
