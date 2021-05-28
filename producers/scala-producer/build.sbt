scalaVersion := "2.13.3"

name := "scala-producer"
organization := "com.mjarosie"
version := "1.0"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  // https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
  "org.apache.kafka" % "kafka-clients" % "2.8.0",
  // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
  "org.slf4j" % "slf4j-simple" % "1.7.30",
)
