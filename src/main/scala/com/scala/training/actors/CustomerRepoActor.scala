package com.scala.training.actors

import akka.actor._
import com.scala.training.domain.{DeleteById, GetById, Insert}
import com.scala.training.repo.{Customer, CustomerRepoT}

/**
  * Created by krishnaveni.n on 05/18/2017.
  */
class CustomerRepoActor(customerRepo:CustomerRepoT) extends Actor with ActorLogging{
  def receive ={
    case Insert(customer:Customer)=>
      customerRepo.insert(customer)
      println("Inserted")
    case GetById(id) =>
      customerRepo.get(id) match {
        case Some(data)=> sender() ! data
        case None=> sender() ! "No Match Found"
      }
      println("sent data")
    case DeleteById(id) => customerRepo.delete(id)

    case  "Show all Customer Details"=> sender() !  customerRepo.getAll.toList
      println("sent")


    case _=>
  }
}