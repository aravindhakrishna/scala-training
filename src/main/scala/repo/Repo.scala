package repo

import com.mongodb.casbah.MongoClient
import com.novus.salat.Context
import domain._


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
