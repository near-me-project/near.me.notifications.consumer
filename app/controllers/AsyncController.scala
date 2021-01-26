package controllers

import akka.actor.ActorSystem
import javax.inject._
import messaging.RabbitClient
import model.{LocationModel, LookupResult}
import play.api.mvc._
import repository.Repository
import shared.GpsLocationUtils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class AsyncController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def locationUpdatesFromClient(clientId: String, latitude: String, longitude: String) = Action.async {
    Future {
      println(s"[Income request >>>] location updates: latitude [$latitude] longitude: [$longitude]")
      processIncomeLocationUpdates(LocationModel(clientId, latitude, longitude)).filter(res => res.nearestPlaces.nonEmpty).map(res => placeMessageToBrokerQueue(res))
      Ok
    }
  }

  private def processIncomeLocationUpdates: LocationModel => Future[LookupResult] = location => {
    val promise = Promise[LookupResult]()

    actorSystem.scheduler.scheduleOnce(1.millisecond) {

      findAllNearestLocations(location).onComplete(promise.complete)

    }(actorSystem.dispatcher)
    promise.future
  }

  private def findAllNearestLocations(incomeReq: LocationModel): Future[LookupResult] = {
    Future {
      val locations: Seq[LocationModel] = new Repository().findByClientId(incomeReq.clientId).filter((location: LocationModel) => checkIfLocationCloseEnough(location, incomeReq))
      println(s"[findAllNearestLocations] Found locations: $locations")
      LookupResult(locations.toList, incomeReq.clientId)
    }
  }

  private def checkIfLocationCloseEnough(fromDb: LocationModel, fromMobile: LocationModel): Boolean = {
    val x1: Double = fromDb.latitude.toDouble
    val y1: Double = fromDb.longitude.toDouble
    val x2: Double = fromMobile.latitude.toDouble
    val y2: Double = fromMobile.longitude.toDouble

    println(s"[checkIfLocationCloseEnough]: distance -> [${GpsLocationUtils.getDistanceBetween(x1, y1, x2, y2)}]")
    GpsLocationUtils.isCloseEnough(x1, y1, x2, y2, 100)
  }

  private def placeMessageToBrokerQueue(result: LookupResult): Unit = RabbitClient.getInstance().sendEventToQueue(result, RabbitClient.MOBILE_NOTIFICATIONS_QUEUE)
}
