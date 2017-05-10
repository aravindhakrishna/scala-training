
import akka.actor._
import org.bson.types.ObjectId
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class ProcessActor(studentRepo:ActorRef)(implicit val exe:ExecutionContext) extends Actor{

  implicit val timeout=Timeout(5000L)
  val uuid=ObjectId.get().toString
  val first=Student(id=uuid ,name = "xxx",age = 25,bloodGroup = "AB+",position = "BE")
  val second=Student(name = "yyy",age = 25,bloodGroup = "AB+",position = "BE")
   def receive ={
     case "2"=> studentRepo ! Insert(first)
     case "3"=> (studentRepo ? "all").mapTo[List[Student]].foreach(println(_))
     case  "4"=> studentRepo ! GetById(uuid)
     case   "5"=>studentRepo ! DeleteById(uuid)
     case "6" => studentRepo ! Insert(second)
     case student:Student=>println(s"Recv $student")
     case "No Match Found"=>println("No Match")
     case Results(data)=> data.foreach(println(_))
     case _=>
   }
}
