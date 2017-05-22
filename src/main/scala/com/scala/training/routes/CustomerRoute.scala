package com.scala.training.routes

import akka.actor.ActorContext
import akka.util.Timeout
import com.scala.training.domain.{DeleteById, GetById, Insert}
import com.scala.training.repo.Customer
import org.bson.types.ObjectId
import play.api.libs.json.Json
import spray.http.StatusCodes
import spray.httpx.PlayJsonSupport
import spray.routing.{Directives, HttpServiceBase}
import akka.pattern.ask

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by krishnaveni.n on 05/18/2017.
  */
trait CustomerRoute extends Directives with HttpServiceBase with PlayJsonSupport{
  implicit val timeout2=Timeout(5000L)
  implicit val _marshall2=Json.writes[Customer]
implicit  val _unmarshall2=Json.reads[Customer]

  def customerActor(context:ActorContext)=context.child("cust-repo").getOrElse(context.system.deadLetters)

  def custRoute(context:ActorContext)(implicit executionContext: ExecutionContext) =  (pathPrefix("cust")){
    pathEndOrSingleSlash{
      get{
        complete(StatusCodes.OK,"hello")
      }
    } ~ pathPrefix("customer") {
      pathEndOrSingleSlash {
        entity(as[Customer]) { data =>
          post { ctx =>
            customerActor(context) ! Insert(data.copy(id = ObjectId.get().toString))
            ctx.complete(StatusCodes.OK, "Customer Created")
          }

        }
      }

    } ~ pathPrefix("customer" / Segment) { input =>
      get { ctx =>
        input match {
          case "Show all Customer Details" => Future {
            (customerActor(context) ? "Show all Customer Details").mapTo[List[Customer]].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK, data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Empty")
            }
          }
          case (x) => Future {
            (customerActor(context) ? GetById(x)).mapTo[Customer].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK, data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Not found any Customer ")
            }
          }
        }
      } ~ delete { ctx =>
        Future {
          (customerActor(context) ? GetById(input)).mapTo[Customer].onComplete {
            case Success(data) =>
              customerActor(context) ! DeleteById(input)
              ctx complete(StatusCodes.OK, "Deleted")
            case Failure(err) => ctx complete(StatusCodes.OK, "Not found any Customer ")
          }
        }
      }
    }
  }
}