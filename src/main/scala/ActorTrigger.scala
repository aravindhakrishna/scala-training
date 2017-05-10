import actors.ProcessActor
import akka.actor.{ActorSystem, Props}
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.typesafe.config.ConfigFactory
import actors.RepoActor
import utils.BootstrapEmbeddedMongo

/**
  * Created by scala on 5/8/17.
  */
object ActorTrigger extends App with BootstrapEmbeddedMongo{

  val config=ConfigFactory.load().getConfig("student")

  def DBHost=config.getString("host")
  def DBPort=config.getInt("port")
  def DBName=config.getString("db.name")

  val system= ActorSystem("student",config = config)
  startupMongo()

  mongoClient=MongoClient(MongoClientURI(config.getString("db.url")))
  val studentRepo=system.actorOf(Props(new RepoActor(mongoClient)),"student-repo")
  val processRepo =system.actorOf(Props(new ProcessActor(studentRepo)(system.dispatcher)),"process-actor")

  while (true){
    readLine() match {
      case "2"=>processRepo ! "2"
      case "3"=>processRepo ! "3"
      case "4"=>processRepo ! "4"
      case "5"=>processRepo ! "5"
      case "6"=>processRepo ! "6"
      case _=> sys.exit(0)
    }
  }

  sys.addShutdownHook{
    shutdownMongo()
    system.shutdown()
  }
}
