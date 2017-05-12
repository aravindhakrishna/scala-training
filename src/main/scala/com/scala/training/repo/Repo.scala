package com.scala.training.repo

import com.mongodb.casbah.MongoClient
import com.novus.salat.Context
import com.novus.salat.annotations.Key
import com.scala.training.domain._
import org.bson.types.ObjectId


case class Student (@Key("_id") id:String=ObjectId.get().toString,
                   name:String,
                   age:Int,
                   bloodGroup:String,
                   position:String)extends Model

trait StudentRepoT extends Repository{
  type Id =String
  type Entity =Student
  type PartialEntity = Student
}

class MongoStudentRepo(val mongoClient: MongoClient,
                             val dbName: String,
                             val collectionName: String)(implicit val context: Context, val idManifest: Manifest[String], val entityManifest: Manifest[Student],
                                                         val partialEntityManifest: Manifest[Student]) extends SalatRepository with StudentRepoT {
  override def id(entity: Student): String = entity.id
}
