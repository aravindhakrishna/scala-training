package com.scala.training.routes

/**
  * Created by krishnaveni.n on 05/18/2017.
  */

import akka.actor.{ActorContext, ActorRef}
import akka.util.Timeout
import com.scala.training.domain._
import com.scala.training.repo.Bank
import org.bson.types.ObjectId
import play.api.libs.json.Json
import spray.http.StatusCodes
import spray.httpx.PlayJsonSupport
import spray.routing.{HttpServiceBase, Directives}
import akka.pattern.ask

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}


trait BankRoute extends Directives with HttpServiceBase with PlayJsonSupport {
  implicit val timeout1 = Timeout(5000L)
  implicit val _marshall1 = Json.writes[Bank]
  implicit val _unmarshall1 = Json.reads[Bank]

  def bankActor(context: ActorContext) = context.child("bank-repo").getOrElse(context.system.deadLetters)

  def bankRoute(context: ActorContext)(implicit executionContext: ExecutionContext) = (pathPrefix("banking")) {
    pathEndOrSingleSlash {
      get {
        complete(StatusCodes.OK, "hello")
      }
    } ~ pathPrefix("bank") {
      pathEndOrSingleSlash {
        entity(as[Bank]) { data =>
          post { ctx =>
            bankActor(context) ! Insert(data.copy(id = ObjectId.get().toString))
            ctx.complete(StatusCodes.OK, "Bank Created")
          }

        }
      }

    } ~ pathPrefix("bank" / Segment) { input =>
      get { ctx =>
        input match {
          case "Show all Bank Details" => Future {
            (bankActor(context) ? "Show all Bank Details").mapTo[List[Bank]].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK, data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Empty")
            }
          }
          case (x) => Future {
            (bankActor(context) ? GetById(x)).mapTo[Bank].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK, data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Not Found any Bank")
            }
          }
        }

      } ~ delete { ctx =>
        Future {
          (bankActor(context) ? GetById(input)).mapTo[Bank].onComplete {
            case Success(data) =>
              bankActor(context) ! DeleteById(input)
              ctx complete(StatusCodes.OK, "Deleted")
            case Failure(err) => ctx complete(StatusCodes.OK, "Not found any bank")
          }
        }
      }
    }
  }

}




