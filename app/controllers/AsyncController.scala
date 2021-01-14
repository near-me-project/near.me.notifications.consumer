package controllers

import akka.actor.ActorSystem
import javax.inject._
import messaging.RabbitClient
import model.{LocationModel, LookupResult}
import play.api.mvc._
import repository.Repository

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class AsyncController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def message(clientId: String, latitude: String, longitude: String) = Action.async {
    Future {
      processIncomeLocationUpdates(LocationModel(clientId, latitude, longitude)).map(res => placeToQueue(res))
      Ok
    }
  }

  private def processIncomeLocationUpdates: LocationModel => Future[LookupResult] = location => {
    val promise = Promise[LookupResult]()

    actorSystem.scheduler.scheduleOnce(1.millisecond) {

      goToDb(location).onComplete(promise.complete)

    }(actorSystem.dispatcher)
    promise.future
  }

  private def goToDb(income: LocationModel): Future[LookupResult] = {
    Future {
      val models: Seq[LocationModel] = new Repository().findByClientId(income.clientId).filter((location: LocationModel) => check(location, income))
      println(s"Found locations: $models")
      LookupResult(models.toList, income.clientId)
    }
  }

  private def check(fromDb: LocationModel, fromMobile: LocationModel) = fromMobile.latitude == fromDb.latitude && fromMobile.longitude == fromDb.longitude

  private def placeToQueue(result: LookupResult): Unit =  RabbitClient.getInstance().sendEventToQueue(result, RabbitClient.MOBILE_NOTIFICATIONS_QUEUE)
}
