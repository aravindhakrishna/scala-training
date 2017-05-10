name := "scala-training"

version := "1.0"

scalaVersion := "2.11.7"


libraryDependencies ++=Seq(
  "com.novus"                  %% "salat"                                 % "1.9.9"          ,
  "de.flapdoodle.embed"         % "de.flapdoodle.embed.mongo"             % "1.47.3"          ,
  "ch.qos.logback"              % "logback-classic"                       % "1.1.3",
  "com.typesafe.akka"          %% "akka-actor"                            % "2.3.14"          ,
  "com.typesafe.akka"          %% "akka-slf4j"                            % "2.3.14"          ,
  "ch.qos.logback"              % "logback-classic"                       % "1.1.3"
)
