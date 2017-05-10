package com.scala.training

import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.novus.salat.global._
import com.scala.training.repo.{MongoStudentRepo, StudentRepoT,Student}
import com.scala.training.utils.BootstrapEmbeddedMongo


object SampleMain extends MongoRepo with App

abstract class MongoRepo  extends BootstrapEmbeddedMongo{
  println("Sample Main Process")
  def buildMongoUri: String = {
    s"mongodb://10.0.2.15:27017/test"
  }
  def DBHost="10.0.2.15"
  def DBPort=27017
  def DBName="student"
  var repo:StudentRepoT=_
  while (true){
    readLine() match {
      case "1"=>startupMongo()
        mongoClient=MongoClient(MongoClientURI(buildMongoUri))
        repo=new MongoStudentRepo(mongoClient,"test","student")
      case "2"=>println(repo.insert(Student(name = "xxx",age = 20,bloodGroup = "AB+",position = "5")))
      case "3" =>repo.getAll.foreach(println(_))
      case _=>sys.exit(0)

    }
  }

  sys.addShutdownHook{
    shutdownMongo()
  }
}

