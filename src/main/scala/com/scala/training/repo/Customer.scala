package com.scala.training.repo

/**
  * Created by krishnaveni.n on 05/18/2017.
  */

import com.mongodb.casbah.MongoClient
import com.novus.salat.Context
import com.novus.salat.annotations.Key
import com.scala.training.domain._
import org.bson.types.ObjectId
case class Customer(
                     @Key("_id") id:String=ObjectId.get().toString,
                     name:String,
                     country:String,
                     Domain:String


                   ) extends Model

trait CustomerRepoT extends Repository{
  type Id =String
  type Entity =Bank
  type PartialEntity = Bank
}

class MongoCustomerRepo(val mongoClient: MongoClient,
                        val dbName: String,
                        val collectionName: String)(implicit val context: Context, val idManifest: Manifest[String], val entityManifest: Manifest[Bank],
                                                    val partialEntityManifest: Manifest[Bank]) extends SalatRepository with CustomerRepoT {
  override def id(entity: Bank): String = entity.id
}