package repository

import model.LocationModel
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, Observable}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class Repository {

  import Repository.locationsCollection

  def findByClientId(clientId: String) = {
    println(s"[Repository] Looking for locations for client $clientId")
    val res: Observable[LocationModel] = locationsCollection.find(equal("clientId", clientId))
      .map(dbo => LocationModel(clientId, dbo.getString("latitude"), dbo.getString("longitude")))
       Await.result(res.toFuture(), Duration.Inf) // TODO: replase with Observable


    /**
     * observable.subscribe ( new Observer[Document] {
     * override def onNext(result: Document): Unit = println(result.toJson())
     * override def onError(e: Throwable): Unit = println("Failed" + e.getMessage)
     * override def onComplete(): Unit = println("Completed")
     * })
     */
  }
}

private object Repository {
  val mongoClient: MongoClient = MongoClient("mongodb://localhost")
  val database: MongoDatabase = mongoClient.getDatabase("near-me-lookup")
  val locationsCollection: MongoCollection[Document] = database.getCollection("locations");
}
