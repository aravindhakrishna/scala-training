package com.scala.training.actors


import akka.actor.SupervisorStrategy.{Resume, Stop, Restart}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.MongoException
import org.bson.types.ObjectId
import com.scala.training.domain._
import com.scala.training.repo.{StudentRepoT, Student}

import scala.concurrent.ExecutionContext

class ProcessActor(studentRepo:StudentRepoT)(implicit val exe:ExecutionContext) extends Actor with ActorLogging{

  implicit val timeout=Timeout(5000L)

  val uuid=ObjectId.get().toString

  val first=Student(id=uuid ,name = "xxx",age = 25,bloodGroup = "AB+",position = "BE")

  val second=Student(name = "yyy",age = 25,bloodGroup = "AB+",position = "BE")

  def studentActor=context.child("student-repo").getOrElse(context.system.deadLetters)


  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3){
    case _:MongoException=>Stop
    case _:Exception=>Restart
  }

  override def preStart(): Unit = {
     startStudent
  }

  def startStudent=context.actorOf(Props(new RepoActor(studentRepo)),"student-repo")

  context.system.log
   def receive ={
     case "2"=> studentActor ! Insert(first)
     case "3"=> (studentActor ? "all").mapTo[List[Student]].foreach(println(_))
     case "4"=> studentActor ! GetById(uuid)
     case "5"=>studentActor ! DeleteById(uuid)
     case "6" => studentActor ! Insert(second)
     case student:Student=>println(s"Recv $student")
     case "No Match Found"=>println("No Match")
     case "student"=>startStudent
     case Results(data)=> data.foreach(println(_))
     case _=>
   }
}

class RepoActor(studentRepo:StudentRepoT) extends Actor with ActorLogging{
  def receive ={
    case Insert(student)=>
      studentRepo.insert(student)
      println("Inserted")
    case GetById(id) =>
      studentRepo.get(id) match {
        case Some(data)=> sender() ! data
        case None=> sender() ! "No Match Found"
      }
      println("sent data")
    case DeleteById(id) =>studentRepo.delete(id)

    case  "all"=> sender() ! studentRepo.getAll.toList
      println("sent")


    case _=>
  }
}
