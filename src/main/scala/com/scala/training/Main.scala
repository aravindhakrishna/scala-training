package com.scala.training

import akka.actor.{ActorSystem, Props}
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.scala.training.actors.{ProcessActor, RepoActor}
import com.scala.training.utils.BootstrapEmbeddedMongo
import com.typesafe.config.ConfigFactory

import scala.io.StdIn


object Main extends App with BootstrapEmbeddedMongo{

  val settings=Settings(ConfigFactory.load().getConfig("student"))

  def DBHost=settings.dbHost
  def DBPort=settings.dbPort
  def DBName=settings.dbName

  val system= ActorSystem("student",settings.conf)
  startupMongo()

  mongoClient=MongoClient(MongoClientURI(settings.dbUrl))
  val studentRepo=system.actorOf(Props(new RepoActor(mongoClient)),"student-com.scala.training.repo")
  val processRepo =system.actorOf(Props(new ProcessActor(studentRepo)(system.dispatcher)),"process-actor")

  while (true){
    StdIn.readLine()  match {
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
