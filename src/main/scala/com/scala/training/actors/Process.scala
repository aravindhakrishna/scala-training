package com.scala.training.actors


import akka.actor._
import com.scala.training.domain._
import com.scala.training.repo.{StudentRepoT}

class RepoActor(studentRepo:StudentRepoT) extends Actor with ActorLogging{
  def receive ={
    case Insert(student)=>
      studentRepo.insert(student)
      println("Inserted")
    case GetById(id) =>
      studentRepo.get(id) match {
        case Some(data)=> sender() ! data
        case None=> sender() ! "No Match Found"
      }
      println("sent data")
    case DeleteById(id) =>studentRepo.delete(id)

    case  "all"=> sender() ! studentRepo.getAll.toList
      println("sent")


    case _=>
  }
}
