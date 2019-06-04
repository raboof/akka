val scala212Version = "2.12.8"

name := "test"

scalacOptions in Compile ++= Seq("-release", "8")

libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.4",
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0",
)
