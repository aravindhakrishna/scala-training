package repo

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.novus.salat.{Context, grater}

import scala.collection.immutable.Seq
import scala.reflect.runtime.universe.typeOf

trait Repository {
  type Id <: AnyRef //Entity's unique identifier (e.g. UUID). May be a tuple or case class in the case of compound primary keys
  type Entity <: AnyRef //Business domain object with a unique identifier
  type PartialEntity <: AnyRef //Container of entity fields that need updating. Usually a case class mimicking the entity but with all optional fields

  def id(entity: Entity): Id //Extracts entity's id for usage in queries
  def get(id: Id): Option[Entity]
  def get(ids: Set[Id]): Seq[Entity]
  def getAll: Seq[Entity]
  def insert(entity: Entity): Unit
  def insert(entities: Seq[Entity]): Unit
  def delete(id: Id): Unit
  def update(id: Id, partialEntity: PartialEntity): Unit
  def upsert(entity: Entity): UpsertStatus

}

sealed trait UpsertStatus
object UpsertStatus {
  case object Updated extends UpsertStatus
  case object Inserted extends UpsertStatus
}

trait SalatRepository extends Repository { self ⇒

  protected def mongoClient: MongoClient
  protected def dbName: String
  protected def collectionName: String
  implicit protected def context: Context
  implicit protected def idManifest: Manifest[Id]
  implicit protected def entityManifest: Manifest[Entity]
  implicit protected def partialEntityManifest: Manifest[PartialEntity]
  lazy protected val salatDao = {
    new SalatDAO[Entity, Id](mongoClient(dbName)(collectionName)) {}
  }

  def get(id: Id): Option[Entity] = {
    //TODO: Salat decided to disable "ById" querying on compound ids to "protect" users who don't read the docs
    //TODO: When they fix it refactor out the boilerplate from this trait
    //TODO: See https://github.com/novus/salat/issues/110 and https://github.com/novus/salat/issues/86
    //    salatDao.findOneById(id)
    salatDao.findOne(idToDBObject(id))
  }

  def get(ids: Set[Id]): Seq[Entity] = {
    salatDao.find(MongoDBObject("_id" -> MongoDBObject("$in" -> MongoDBList(ids.toList: _*)))).toIndexedSeq
  }

  def getAll: Seq[Entity] =
    salatDao.find(MongoDBObject.empty).toList

  def insert(entity: Entity): Unit =
    salatDao.insert(entity)

  def insert(entities: Seq[Entity]): Unit =
    salatDao.insert(entities)

  def delete(id: Id): Unit = {
    //    salatDao.removeById(id)
    salatDao.remove(idToDBObject(id))
  }

  def update(id: Id, partialEntity: PartialEntity): Unit = {
    val dbObject = grater[PartialEntity].asDBObject(partialEntity)
    salatDao.update(idToDBObject(id), $set(dbObject.toSeq: _*), upsert = false)
  }

  def upsert(entity: Entity): UpsertStatus = {
    val writeResult = salatDao.update(idToDBObject(id(entity)), salatDao.toDBObject(entity), upsert = true)
    if (writeResult.isUpdateOfExisting) UpsertStatus.Updated else UpsertStatus.Inserted
  }

  private type CaseClass = AnyRef with Product

  def idToDBObject(id: Id): DBObject =
    if (typeOf[Id] <:< typeOf[CaseClass]) DBObject("_id" -> grater[Id].asDBObject(id)) else DBObject("_id" -> id)

}