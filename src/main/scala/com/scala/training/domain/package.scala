package com.scala.training

import com.scala.training.repo.Employee


package object domain {



  case class Insert(empolyee: Employee)

  case class GetById(id:String)

  case class DeleteById(id:String)

  case class Results(employees:List[Employee])
}
