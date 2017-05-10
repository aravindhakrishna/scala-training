import java.util.UUID

import com.novus.salat.global._
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import repo.{MongoStudentRepo, StudentRepoT}
import domain._
import utils.BootstrapEmbeddedMongo


object Training extends MongoRepo with App{
  println("test")

}


abstract class MongoRepo  extends BootstrapEmbeddedMongo{
  def buildMongoUri: String = {
    s"mongodb://10.0.2.15:27017/test"
  }
  def DBHost="10.0.2.15"
  def DBPort=27017
  def DBName="student"
  var repo:StudentRepoT=_
  while (true){
    readLine() match {
      case "1"=>startupMongo()
        mongoClient=MongoClient(MongoClientURI(buildMongoUri))
        repo=new MongoStudentRepo(mongoClient,"test","student")
      case "2"=>println(repo.insert(Student(name = "xxx",age = 20,bloodGroup = "AB+",position = "5")))
      case "3" =>repo.getAll.foreach(println(_))
      case _=>sys.exit(0)

    }
  }

  sys.addShutdownHook{
    shutdownMongo()
  }
}

