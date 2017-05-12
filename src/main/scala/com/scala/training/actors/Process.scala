package com.scala.training.actors


import akka.actor.SupervisorStrategy.{Resume, Stop, Restart}
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.MongoException
import org.bson.types.ObjectId
import com.scala.training.domain._
import com.scala.training.repo.{StudentRepoT, Student}
import com.scala.training.repo.{CustomerRepoT, Customer}
import scala.concurrent.ExecutionContext

class ProcessActor(studentRepo:StudentRepoT,customerRepo:CustomerRepoT)(implicit val exe:ExecutionContext) extends Actor with ActorLogging{

  implicit val timeout=Timeout(5000L)

  val uuid=ObjectId.get().toString

  val first=Student(id=uuid ,name = "xxx",age = 25,bloodGroup = "AB+",position = "BE")

  val second=Student(name = "yyy",age = 25,bloodGroup = "AB+",position = "BE")

  val cust1=Customer(id=uuid ,name = "Symscribe",country="US",Domain = "Angular & Nodejs")
  val cust2=Customer(name = "Rico",country= "UK", Domain = "Aangular & Asp.net")
  val cust3=Customer(name = "Viridity",country="UK",Domain = "Aangular & Asp.net")


  def studentActor=context.child("student-repo").getOrElse(context.system.deadLetters)

  def customerActor=context.child("customer-repo").getOrElse(context.system.deadLetters)

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3){
    case _:MongoException=>Stop
    case _:Exception=>Restart
  }

  override def preStart(): Unit = {
     startStudent
    startCustomer
  }

  def startStudent=context.actorOf(Props(new StudentRepoActor(studentRepo)),"student-repo")
  def startCustomer=context.actorOf(Props(new CustomerRepoActor(customerRepo)),"customer-repo")

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

     case "a"=> customerActor ! Insert(cust1)
     case "b"=> (customerActor ? "all").mapTo[List[Customer]].foreach(println(_))
     case "c"=> customerActor ! GetById(uuid)
     case "d"=>customerActor ! DeleteById(uuid)
     case "e" => customerActor ! Insert(cust2)
     case customer:Customer=>println(s"Recv $customer")
     case "No Match Found"=>println("No Match")
     case "customer"=>startCustomer


     case _=>
   }
}

class StudentRepoActor(studentRepo:StudentRepoT) extends Actor with ActorLogging{
  def receive ={
    case Insert(student:Student)=>
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

class CustomerRepoActor(customerRepo:CustomerRepoT) extends Actor with ActorLogging{
  def receive ={
    case Insert(customer:Customer)=>
      customerRepo.insert(customer)
      println("Inserted")
    case GetById(id) =>
      customerRepo.get(id) match {
        case Some(data)=> sender() ! data
        case None=> sender() ! "No Match Found"
      }
      println("sent data")
    case DeleteById(id) => customerRepo.delete(id)

    case  "all"=> sender() !  customerRepo.getAll.toList
      println("sent")


    case _=>
  }
}

