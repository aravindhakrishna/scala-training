package com.scala.training

import com.novus.salat.annotations.Key
import org.bson.types.ObjectId


package object domain {

  case class Student(@Key("_id") id:String=ObjectId.get().toString,
                     name:String,
                     age:Int,
                     bloodGroup:String,
                     position:String)

  case class Insert(student: Student)

  case class GetById(id:String)

  case class DeleteById(id:String)

  case class Results(students:List[Student])
}
