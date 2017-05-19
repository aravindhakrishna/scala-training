package com.scala.training

import com.scala.training.repo.Employee


package object domain {
  trait  Model



  case class Insert(model: Model)


  case class GetById(id:String)

  case class DeleteById(id:String)

  case class Results(models:List[Model])

}
