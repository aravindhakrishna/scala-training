package com.scala.training.actors


import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.casbah.MongoClient
import org.bson.types.ObjectId
import com.scala.training.domain._
import com.scala.training.repo.MongoStudentRepo

import scala.concurrent.ExecutionContext

class ProcessActor(studentRepo:ActorRef)(implicit val exe:ExecutionContext) extends Actor{

  implicit val timeout=Timeout(5000L)
  val uuid=ObjectId.get().toString
  val first=Student(id=uuid ,name = "xxx",age = 25,bloodGroup = "AB+",position = "BE")
  val second=Student(name = "yyy",age = 25,bloodGroup = "AB+",position = "BE")
   def receive ={
     case "2"=> studentRepo ! Insert(first)
     case "3"=> (studentRepo ? "all").mapTo[List[Student]].foreach(println(_))
     case  "4"=> studentRepo ! GetById(uuid)
     case   "5"=>studentRepo ! DeleteById(uuid)
     case "6" => studentRepo ! Insert(second)
     case student:Student=>println(s"Recv $student")
     case "No Match Found"=>println("No Match")
     case Results(data)=> data.foreach(println(_))
     case _=>
   }
}

class RepoActor(mongoClient:MongoClient) extends Actor{

  import com.novus.salat.global._
  var repo:MongoStudentRepo=_


  override def preStart(): Unit = {
    repo=new MongoStudentRepo(mongoClient,"test","student")
  }


  def receive ={
    case Insert(student)=>
      repo.insert(student)
      println("Inserted")
    case GetById(id) =>
      repo.get(id) match {
        case Some(data)=> sender() ! data
        case None=> sender() ! "No Match Found"
      }
      println("sent data")
    case DeleteById(id) =>repo.delete(id)

    case  "all"=> sender() ! repo.getAll.toList
      println("sent")


    case _=>
  }
}
