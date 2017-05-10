
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.TypeImports._

import de.flapdoodle.embed.mongo.{ MongodProcess, MongodExecutable, MongodStarter }
import de.flapdoodle.embed.mongo.config.{MongoCmdOptionsBuilder, Net, MongodConfigBuilder, RuntimeConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.io.{ NullProcessor, Processors }
import de.flapdoodle.embed.process.runtime.Network

import scala.concurrent._
import scala.concurrent.duration._
import de.flapdoodle.embed.mongo.Command

import java.util.UUID

trait BootstrapEmbeddedMongo extends TestSupport {
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  var mongoStarter: MongodStarter = _
  var mongoExe: MongodExecutable = _
  var mongod: MongodProcess = _
  var mongoClient:MongoClient=_

  def DBHost: String
  def DBPort: Int
  def DBName: String

  def startupMongo() {
    val mongoLocalHostIPV6 = Network.localhostIsIPv6()

    // Used to filter out console output messages.
    val processOutput = new ProcessOutput(
      Processors.named("[mongod>]", new NullProcessor),
      Processors.named("[MONGOD>]", new NullProcessor),
      Processors.named("[console>]", new NullProcessor))

    val runtimeConfig = new RuntimeConfigBuilder()
      .defaults(Command.MongoD)
      .processOutput(processOutput)
      .build()

    // Start mongo instance
    mongoStarter = MongodStarter.getInstance(runtimeConfig)
    val mongodConfig = new MongodConfigBuilder().cmdOptions(new MongoCmdOptionsBuilder().useNoJournal(false).build()).version(Version.Main.V3_0).net(new Net(DBHost,DBPort,mongoLocalHostIPV6)).build()
    mongoExe = mongoStarter.prepare(mongodConfig)


    retryUntil("mongo starts up", 30 seconds, 5 seconds) {
      mongod = mongoExe.start()
    }

    mongoClient = MongoClient(DBHost,DBPort)

    val db = mongoClient(DBName)
    val collection = db("testdb")
    val id = UUID.randomUUID()

    retryUntil("a collection to be created", 30 seconds) {
      val wr = collection.insert(DBObject("id" -> id), WriteConcern.FsyncSafe)

      val qry = DBObject("id" -> id)
      assert(collection.find(qry).size > 0, "Failed on creating collection")
    }
  }

  def shutdownMongo() {
    implicit val timeout = 30.seconds

    cleanupMongo()

    retryUntil("Mongo db shuts down", timeout, 1 second) {
      mongoClient.close
      mongod.stop()
      mongoExe.stop()
    }
  }

  def cleanupMongo() {
    try {
      val db = mongoClient(DBName)
      db.dropDatabase
    }
    catch {
      case e: Throwable ⇒
    }

  }
}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

class TestTimeoutException(description: String, elapsed: Double, nestedException: Throwable) extends Exception(
  s"Timed out after ${elapsed} seconds while waiting until ${description}", nestedException) {
  override def getMessage = getCause.getMessage
}

/**
  * Provides utilities for unit and integration tests.
  */
trait TestSupport {


  def retryUntil(description: String, timeout: Duration, pause: Duration = 100 milliseconds)(func: ⇒ Unit) {
    def now = System.currentTimeMillis()
    val start = now
    val end = start + timeout.toUnit(TimeUnit.MILLISECONDS)

    def evalFunction(f: ⇒ Unit) = {
      scala.util.control.Exception.allCatch either f
    }

    var lastResult = evalFunction(func)
    while (now <= end && lastResult.isLeft) {
      Thread.sleep(pause.toMillis)
      lastResult = evalFunction(func)
    }

    val elapsed = (now - start) / 1000.0
    if (now > end) {
      throw new TestTimeoutException(description, elapsed, lastResult.left.get)
    }
  }
}