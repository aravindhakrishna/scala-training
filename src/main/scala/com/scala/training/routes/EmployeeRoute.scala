package com.scala.training.routes

import akka.actor.{ActorContext, ActorRef}
import akka.util.Timeout
import com.scala.training.domain._
import com.scala.training.repo.Employee
import org.bson.types.ObjectId
import play.api.libs.json.Json
import spray.http.StatusCodes
import spray.httpx.PlayJsonSupport
import spray.routing.{HttpServiceBase, Directives}
import akka.pattern.ask

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}


trait EmployeeRoute extends Directives with HttpServiceBase with PlayJsonSupport{
  implicit val timeout=Timeout(5000L)
  implicit val _marshall=Json.writes[Employee]
  val uuid=ObjectId.get().toString

  val first=Employee(id=uuid ,name = "Krishna",role= "Developer",department="Digital",project="Viridity",age=24)
  val second=Employee(name = "yuva",role= "Developer",department="Digital",project="Vpower",age=26)
  val temp=Employee(name="Thara",role="QA",department="SAP",project="SFL",age=25)
  def third=Employee(name = "priya"+uuid.codePointCount(4,8),role="QA",department="ABAP",project="SFL",age=23)

  def employeeActor(context:ActorContext)=context.child("emp-repo").getOrElse(context.system.deadLetters)

  def testRoute(context:ActorContext)(implicit executionContext: ExecutionContext) =  (pathPrefix("base")){
    pathEndOrSingleSlash{
     get{
       complete(StatusCodes.OK,"hello")
      }
    } ~  pathPrefix("next"/Segment){input=>
      get{ctx=>
            input match {
              case "a"=> employeeActor(context) ! Insert(first)
                ctx complete(StatusCodes.OK," One Employee created")
              case "b"=>Future {
                (employeeActor(context) ? "Show all Emplyee Details").mapTo[List[Employee]].onComplete {
                  case Success(data) => ctx complete(StatusCodes.OK,data)
                  case Failure(err) => ctx complete(StatusCodes.OK, "Empty")
                }
              }
              case "c"=>Future {
                (employeeActor(context) ? GetById(uuid)).mapTo[Employee].onComplete {
                  case Success(data) => ctx complete(StatusCodes.OK,data)
                  case Failure(err) => ctx complete(StatusCodes.OK, "Not Found an Employee")
                }
              }
              case "d"=>employeeActor(context) ! DeleteById(first.id)
                ctx complete(StatusCodes.OK,"One Employee Deleted")
              case "e"=>employeeActor(context) ! Insert(second)
                ctx complete(StatusCodes.OK,"One Employee created")
              case "f"=>employeeActor(context) ! Insert(third)
                ctx complete(StatusCodes.OK,"One Employee created")
              case (x)=>Future {
                (employeeActor(context) ? GetById(x)).mapTo[Employee].onComplete {
                  case Success(data) => ctx complete(StatusCodes.OK,data)
                  case Failure(err) => ctx complete(StatusCodes.OK, "Not found an Employee")
                }
              }
            }
      }
    }
  }
}

