val scala212Version = "2.12.8"

scalacOptions in Compile ++= (
    // -release 8 is not enough, for some reason we need the 8 rt.jar explicitly #25330
    Seq("-release", "8"))

libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.4",
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
    compilerPlugin("com.github.ghik" %% "silencer-plugin" % "1.3.3"),
    "com.github.ghik" %% "silencer-lib" % "1.3.3" % "provided",
)
