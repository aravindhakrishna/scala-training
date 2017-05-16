package com.scala.training.actors

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Props, OneForOneStrategy, SupervisorStrategy, ActorRef}
import akka.io.IO
import com.mongodb.MongoException
import com.scala.training.repo.{StudentRepoT, Student}
import com.scala.training.routes.RestRoute
import org.bson.types.ObjectId
import spray.can.Http
import spray.routing.HttpServiceActor


class WebServiceActor(host:String,port:Int,studentRepoT: StudentRepoT) extends HttpServiceActor with RestRoute{

  import context.dispatcher

  IO(Http)(context.system) ! Http.Bind(listener = self, interface = host, port = port)

  def startStudent=context.actorOf(Props(new RepoActor(studentRepoT)),"student-repo")

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3){
    case _:Exception=>Restart
  }

  override def preStart() ={
    startStudent
  }

 def receive = runRoute{
   import  context.dispatcher
   testRoute(context)}
}
