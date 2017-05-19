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

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by krishnaveni.n on 05/18/2017.
  */
trait CustomerRoute extends Directives with HttpServiceBase with PlayJsonSupport{
  implicit val timeout=Timeout(5000L)
  implicit val _marshall=Json.writes[Customer]
  val uuid=ObjectId.get().toString



  val cust1=Customer(id=uuid ,name = "Symscribe",country="US",Domain = "Angular & Nodejs")
  val cust2=Customer(name = "Rico",country= "UK", Domain = "Angular & Asp.net")
  val cust3=Customer(name = "Viridity",country="UK",Domain = "Angular & Akka")
  def cust4=Customer(name = "VPower"+uuid.codePointCount(4,8),country="Sweden",Domain = "Angular & Scala")

  def customerActor(context:ActorContext)=context.child("cust-repo").getOrElse(context.system.deadLetters)

  def custRoute(context:ActorContext)(implicit executionContext: ExecutionContext) =  (pathPrefix("cust")){
    pathEndOrSingleSlash{
      get{
        complete(StatusCodes.OK,"hello")
      }
    } ~  pathPrefix("next"/Segment){input=>
      get{ctx=>
        input match {
          case "1"=> customerActor(context) ! Insert(cust1)
            ctx complete(StatusCodes.OK," One Customer created")
          case "2"=>Future {
            (customerActor(context) ? "Show all Customer Details").mapTo[List[Customer]].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK,data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Empty")
            }
          }
          case "3"=>Future {
            (customerActor(context) ? GetById(uuid)).mapTo[Customer].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK,data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Not Found an Customer")
            }
          }
          case "4"=>customerActor(context) ! DeleteById(cust1.id)
            ctx complete(StatusCodes.OK,"One Customer Deleted")
          case "5"=>customerActor(context) ! Insert(cust2)
            ctx complete(StatusCodes.OK,"One Customer created")
          case "6"=>customerActor(context) ! Insert(cust2)
            ctx complete(StatusCodes.OK,"One Customer created" )
          case "7"=>customerActor(context) ! Insert(cust4)
            ctx complete(StatusCodes.OK,"One Customer created")
          case (x)=>Future {
            (customerActor(context) ? GetById(x)).mapTo[Customer].onComplete {
              case Success(data) => ctx complete(StatusCodes.OK,data)
              case Failure(err) => ctx complete(StatusCodes.OK, "Not found the Customer")
            }
          }
        }
      }
    }
  }
}

