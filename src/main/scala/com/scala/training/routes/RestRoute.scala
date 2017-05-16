package com.scala.training.routes

import akka.actor.{ActorContext, ActorRef}
import akka.util.Timeout
import com.scala.training.domain._
import com.scala.training.repo.Student
import org.bson.types.ObjectId
import play.api.libs.json.Json
import spray.http.StatusCodes
import spray.httpx.PlayJsonSupport
import spray.routing.{HttpServiceBase, Directives}
import akka.pattern.ask

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}


trait RestRoute extends Directives with HttpServiceBase with PlayJsonSupport{
  implicit val timeout=Timeout(5000L)
  implicit val _marshall=Json.writes[Student]
  val uuid=ObjectId.get().toString

  val first=Student(id=uuid ,name = "xxx",age = 25,bloodGroup = "AB+",position = "BE")

  val second=Student(name = "yyy",age = 25,bloodGroup = "AB+",position = "BE")

  def studentActor(context:ActorContext)=context.child("student-repo").getOrElse(context.system.deadLetters)

  def testRoute(context:ActorContext)(implicit executionContext: ExecutionContext) =  (pathPrefix("test")){
    pathEndOrSingleSlash{
     get{
       complete(StatusCodes.OK,"hello")
      }
    } ~  pathPrefix("rest"/Segment){input=>
      get{ctx=>
            input match {
              case "2"=> studentActor(context) ! Insert(first)
                ctx complete(StatusCodes.OK,"Inserted")
              case "3"=>Future {
                (studentActor(context) ? "all").mapTo[List[Student]].onComplete {
                  case Success(data) => ctx complete(StatusCodes.OK,data)
                  case Failure(err) => ctx complete(StatusCodes.OK, "Non Matched")
                }
              }
              case "5"=>studentActor(context) ! DeleteById(first.id)
                ctx complete(StatusCodes.OK,"Deleted")
              case "6"=>studentActor(context) ! Insert(second)
                ctx complete(StatusCodes.OK,"Inserted")
            }
      }
    }
  }
}

