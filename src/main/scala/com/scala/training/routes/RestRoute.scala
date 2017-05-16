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
  implicit val _unmarshall=Json.reads[Student]

  def studentActor(context:ActorContext)=context.child("student-repo").getOrElse(context.system.deadLetters)

  def testRoute(context:ActorContext)(implicit executionContext: ExecutionContext) =  (pathPrefix("rest")){
    pathEndOrSingleSlash{
     get{
       complete(StatusCodes.OK,"hello")
      }
    } ~  pathPrefix("student"){
      pathEndOrSingleSlash{
        entity(as[Student]){data=>
          post{ctx=>
            studentActor(context) ! Insert(data.copy(id=ObjectId.get().toString))
            ctx.complete(StatusCodes.OK,"Inserted")
          }

        }
      }

    } ~  pathPrefix("student"/Segment){input=>
      get{ctx=>
            input match {
              case "all"=>Future {
                (studentActor(context) ? "all").mapTo[List[Student]].onComplete {
                  case Success(data) => ctx complete(StatusCodes.OK,data)
                  case Failure(err) => ctx complete(StatusCodes.OK, "Empty")
                }
              }
              case (x)=>Future {
                (studentActor(context) ? GetById(x)).mapTo[Student].onComplete {
                  case Success(data) => ctx complete(StatusCodes.OK,data)
                  case Failure(err) => ctx complete(StatusCodes.OK, "Not Matched")
                }
              }
            }
      } ~ delete{ctx=>
        Future {
          (studentActor(context) ? GetById(input)).mapTo[Student].onComplete {
            case Success(data) =>
              studentActor(context) ! DeleteById(input)
              ctx complete(StatusCodes.OK,"Deleted")
            case Failure(err) => ctx complete(StatusCodes.OK, "Not Matched")
          }
        }
      }
    }
  }
}

