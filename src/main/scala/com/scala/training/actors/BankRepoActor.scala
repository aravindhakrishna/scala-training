package com.scala.training.actors

/**
  * Created by krishnaveni.n on 05/18/2017.
  */
import akka.actor._
import com.scala.training.domain.{DeleteById, GetById, Insert}
import com.scala.training.repo.{Bank, BankRepoT}

/**
  * Created by krishnaveni.n on 05/18/2017.
  */
class BankRepoActor(bankRepo:BankRepoT) extends Actor with ActorLogging{
  def receive ={
    case Insert(bank:Bank)=>
      bankRepo.insert(bank)
      println("Inserted")
    case GetById(id) =>
      bankRepo.get(id) match {
        case Some(data)=> sender() ! data
        case None=> sender() ! "No Match Found"
      }
      println("sent data")
    case DeleteById(id) => bankRepo.delete(id)

    case  "Show all Bank Details"=> sender() !  bankRepo.getAll.toList
      println("sent")


    case _=>
  }
}