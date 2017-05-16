package com.scala.training

import akka.actor.{ActorSystem, Props}
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.scala.training.actors.{WebServiceActor}
import com.scala.training.repo.MongoStudentRepo
import com.scala.training.utils.BootstrapEmbeddedMongo
import com.typesafe.config.ConfigFactory

import com.novus.salat.global._
import scala.io.StdIn


object Main extends App with BootstrapEmbeddedMongo{

  val settings=Settings(ConfigFactory.load().getConfig("application"))

  def DBHost=settings.dbHost
  def DBPort=settings.dbPort
  def DBName=settings.dbName

  val system= ActorSystem("application-system",settings.conf)
  startupMongo()

  mongoClient=MongoClient(MongoClientURI(settings.dbUrl))
  val studentRepo=new MongoStudentRepo(mongoClient,DBName,settings.dbTable)
  system.actorOf(Props(new WebServiceActor(settings.host,settings.port,studentRepo)),"external-host")


  sys.addShutdownHook{
    shutdownMongo()
    system.shutdown()
  }
}
