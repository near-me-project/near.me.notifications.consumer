package repository

import org.mongodb.scala.model.Filters._
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

class Repository {

  import Repository.locationsCollection

  def findByClientId(clientId: String) = {
    locationsCollection.find(equal("clientId", clientId))
  }
}

private object Repository {
  val mongoClient: MongoClient = MongoClient("mongodb://localhost")
  val database: MongoDatabase = mongoClient.getDatabase("near-me-lookup")
  val locationsCollection: MongoCollection[Document] = database.getCollection("locations");
}
