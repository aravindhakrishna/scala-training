package com.scala.training.actors

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import akka.io.IO
import akka.util.Timeout
import com.mongodb.MongoException
import com.scala.training.repo.{Bank, BankRepoT, Customer, CustomerRepoT, Employee, EmployeeRepoT}
import com.scala.training.routes.{BankRoute, CustomerRoute, EmployeeRoute}
import org.bson.types.ObjectId
import spray.can.Http
import spray.routing.HttpServiceActor


trait All_Routes extends EmployeeRoute with BankRoute with CustomerRoute

class WebServiceActor(host: String, port: Int, employeeRepo: EmployeeRepoT, bankRepo: BankRepoT, customerRepo: CustomerRepoT) extends HttpServiceActor with All_Routes  {

  import context.dispatcher

    IO(Http)(context.system) ! Http.Bind(listener = self, interface = host, port = port)

    def startEmployee = context.actorOf(Props(new EmployeeRepoActor(employeeRepo)), "emp-repo")
    def startBank = context.actorOf(Props(new BankRepoActor(bankRepo)),"bank-repo")
    def startCustomer = context.actorOf(Props(new CustomerRepoActor(customerRepo)), "cust-repo")



     override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3) {
      case _: Exception => Restart
    }

    override def preStart() = {
      startEmployee
      startBank
      startCustomer

    }

    def receive = runRoute {
      import context.dispatcher
      testRoute(context) ~ bankRoute(context) ~ custRoute(context)
    }
  }

