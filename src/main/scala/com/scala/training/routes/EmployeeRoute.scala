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


trait EmployeeRoute extends Directives with HttpServiceBase with PlayJsonSupport {
  implicit val timeout = Timeout(5000L)
  implicit val _marshall = Json.writes[Employee]
  implicit val _unmarshall = Json.reads[Employee]


  def employeeActor(context: ActorContext) = context.child("emp-repo").getOrElse(context.system.deadLetters)

  def testRoute(context: ActorContext)(implicit executionContext: ExecutionContext) = (pathPrefix("base")) {
    pathEndOrSingleSlash {
      get {
        complete(StatusCodes.OK, "hello")
      }
    } ~ pathPrefix("employee") {
      pathEndOrSingleSlash {
        entity(as[Employee]) { data =>
          post { ctx =>
            employeeActor(context) ! Insert(data.copy(id = ObjectId.get().toString))
            ctx.complete(StatusCodes.OK, "Employee Created")
          }

        }
      }

    } ~ pathPrefix("employee" / Segment) { input =>
      get { ctx =>
        input match {
          case "Show all Employee Details" => Future {
            (employeeActor(context) ? "Show all Employee Details").mapTo[List[Employee]].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK, data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Empty")
            }
          }
          case (x) => Future {
            (employeeActor(context) ? GetById(x)).mapTo[Employee].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK, data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Not Found an Employee")
            }
          }
        }
      } ~ delete { ctx =>
        Future {
          (employeeActor(context) ? GetById(input)).mapTo[Employee].onComplete {
            case Success(data) =>
              employeeActor(context) ! DeleteById(input)
              ctx complete(StatusCodes.OK, "Employee Deleted")
            case Failure(err) => ctx complete(StatusCodes.OK, "Not found an Employee")
          }
        }
      }
    }
  }
}