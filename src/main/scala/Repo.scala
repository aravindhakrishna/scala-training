import akka.actor.Actor
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.novus.salat.Context
import com.novus.salat.annotations._
import com.typesafe.config.Config
import org.bson.types.ObjectId

case class Student(@Key("_id") id:String=ObjectId.get().toString,
                   name:String,
                   age:Int,
                   bloodGroup:String,
                   position:String)

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

case class Insert(student: Student)
case class GetById(id:String)
case class DeleteById(id:String)
case class Results(students:List[Student])

class RepoActor(mongoClient:MongoClient) extends Actor{

  import com.novus.salat.global._
  var repo:MongoStudentRepo=_


  override def preStart(): Unit = {
    repo=new MongoStudentRepo(mongoClient,"test","student")
  }


  def receive ={
    case Insert(student)=>
          repo.insert(student)
      println("Inserted")
    case GetById(id) =>
         repo.get(id) match {
           case Some(data)=> sender() ! data
           case None=> sender() ! "No Match Found"
         }
      println("sent data")
    case DeleteById(id) =>repo.delete(id)

     case  "all"=> sender() ! repo.getAll.toList
      println("sent")


     case _=>
  }
}
