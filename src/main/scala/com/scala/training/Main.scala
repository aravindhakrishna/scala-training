package com.scala.training

import akka.actor.{ActorSystem, Props}
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.scala.training.actors.{ProcessActor, RepoActor}
import com.scala.training.utils.BootstrapEmbeddedMongo
import com.typesafe.config.ConfigFactory


object Main extends App with BootstrapEmbeddedMongo{

  val config=ConfigFactory.load().getConfig("student")

  def DBHost=config.getString("host")
  def DBPort=config.getInt("port")
  def DBName=config.getString("db.name")

  val system= ActorSystem("student",config = config)
  startupMongo()

  mongoClient=MongoClient(MongoClientURI(config.getString("db.url")))
  val studentRepo=system.actorOf(Props(new RepoActor(mongoClient)),"student-com.scala.training.repo")
  val processRepo =system.actorOf(Props(new ProcessActor(studentRepo)(system.dispatcher)),"process-actor")

  while (true){
    readLine() match {
      case "2"=>processRepo ! "2"
      case "3"=>processRepo ! "3"
      case "4"=>processRepo ! "4"
      case "5"=>processRepo ! "5"
      case "6"=>processRepo ! "6"
      case _=> sys.exit(0)
    }
  }

  sys.addShutdownHook{
    shutdownMongo()
    system.shutdown()
  }
}
