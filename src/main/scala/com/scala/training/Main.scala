package com.scala.training

import akka.actor.{ActorSystem, Props}
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.scala.training.actors.{ProcessActor}
import com.scala.training.repo.MongoStudentRepo
import com.scala.training.utils.BootstrapEmbeddedMongo
import com.typesafe.config.ConfigFactory

import com.novus.salat.global._
import scala.io.StdIn


object Main extends App with BootstrapEmbeddedMongo{

  val settings=Settings(ConfigFactory.load().getConfig("student"))

  def DBHost=settings.dbHost
  def DBPort=settings.dbPort
  def DBName=settings.dbName

  val system= ActorSystem("student",settings.conf)
  startupMongo()

  mongoClient=MongoClient(MongoClientURI(settings.dbUrl))
  val studentRepo=new MongoStudentRepo(mongoClient,DBName,settings.dbTable)
  val processRepo =system.actorOf(Props(new ProcessActor(studentRepo)(system.dispatcher)),"process-actor")

  while (true){
    StdIn.readLine()  match {
      case "2"=>processRepo ! "2"
      case "3"=>processRepo ! "3"
      case "4"=>processRepo ! "4"
      case "5"=>processRepo ! "5"
      case "6"=>processRepo ! "6"
      case "7"=> processRepo ! "student"
      case _=> sys.exit(0)
    }
  }

  sys.addShutdownHook{
    shutdownMongo()
    system.shutdown()
  }
}
