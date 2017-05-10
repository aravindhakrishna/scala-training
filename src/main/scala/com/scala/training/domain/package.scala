package com.scala.training

import com.scala.training.repo.Student


package object domain {



  case class Insert(student: Student)

  case class GetById(id:String)

  case class DeleteById(id:String)

  case class Results(students:List[Student])
}
