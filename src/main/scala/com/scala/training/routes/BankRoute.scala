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


trait BankRoute extends Directives with HttpServiceBase with PlayJsonSupport{
  implicit val timeout=Timeout(5000L)
  implicit val _marshall=Json.writes[Bank]
  val uuid=ObjectId.get().toString

  val bank1=Bank(id=uuid ,name = "StateBank",branch= "Pudur",city="Pollachi",ifsc_code="SBI004315")
  val bank2=Bank(name = "KVB",branch= "Ericinampatti",city="Udumalpet",ifsc_code="KVB003210")
  val bank3=Bank(name="Syndicate",branch="Kolarpatti",city="Pollachi",ifsc_code="SYN006789")
  def bank4=Bank(name = "HDFC"+uuid.codePointCount(4,8),branch="PTC",city="Chennai",ifsc_code="HDFC007812")

  def bankActor(context:ActorContext)=context.child("bank-repo").getOrElse(context.system.deadLetters)

  def bankRoute(context:ActorContext)(implicit executionContext: ExecutionContext) =  (pathPrefix("bank")){
    pathEndOrSingleSlash{
      get{
        complete(StatusCodes.OK,"hello")
      }
    } ~  pathPrefix("next"/Segment){input=>
      get{ctx=>
        input match {
          case "A"=> bankActor(context) ! Insert(bank1)
            ctx complete(StatusCodes.OK," One Bank created")
          case "B"=>Future {
            (bankActor(context) ? "Show all Bank Details").mapTo[List[Bank]].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK,data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Empty")
            }
          }
          case "C"=>Future {
            (bankActor(context) ? GetById(uuid)).mapTo[Bank].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK,data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Not Found an Employee")
            }
          }
          case "D"=>bankActor(context) ! DeleteById(bank1.id)
            ctx complete(StatusCodes.OK,"One Bank Deleted")
          case "E"=>bankActor(context) ! Insert( bank2)
            ctx complete(StatusCodes.OK,"One Bank created")
          case "F"=>bankActor(context) ! Insert( bank3)
            ctx complete(StatusCodes.OK,"One Bank created")
          case "G"=>bankActor(context) ! Insert(bank4)
            ctx complete(StatusCodes.OK,"One Bank created")
          case (x)=>Future {
            (bankActor(context) ? GetById(x)).mapTo[Bank].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK,data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Not found any Bank")
            }
          }
        }
      }
    }
  }
}


