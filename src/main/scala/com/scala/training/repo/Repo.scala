package com.scala.training.repo

import com.mongodb.casbah.MongoClient
import com.novus.salat.Context
import com.novus.salat.annotations.Key
import com.scala.training.domain._
import org.bson.types.ObjectId

case class Employee(
                     @Key("_id") id:String=ObjectId.get().toString,
                     name:String,
                     role:String,
                     department:String,
                     project:String,
                     age:Int
                   )
trait EmployeeRepoT extends Repository{
  type Id =String
  type Entity =Employee
  type PartialEntity = Employee
}
class MongoEmployeeRepo(val mongoClient: MongoClient,
                        val dbName: String,
                        val collectionName: String)(implicit val context: Context, val idManifest: Manifest[String], val entityManifest: Manifest[Employee],
                                                    val partialEntityManifest: Manifest[Employee]) extends SalatRepository with EmployeeRepoT {
  override def id(entity: Employee): String = entity.id
}
