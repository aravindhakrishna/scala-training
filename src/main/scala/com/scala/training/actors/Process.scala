package com.scala.training.actors


import akka.actor._
import com.scala.training.domain._
import com.scala.training.repo.{EmployeeRepoT}



class RepoActor(employeeRepo:EmployeeRepoT) extends Actor with ActorLogging{

    def receive ={
    case Insert(employee)=>
      employeeRepo.insert(employee)
      println("Employee created")
    case GetById(id) =>
      employeeRepo.get(id) match {
        case Some(data)=> sender() ! data
        case None=> sender() ! "No Match Found"
      }
      println("only one employee data sent ")
    case DeleteById(id) =>employeeRepo.delete(id)
      println("Employee deleted")

    case  "all"=> sender() ! employeeRepo.getAll.toList
      println("All employee details sent")


    case _=>
  }
}
