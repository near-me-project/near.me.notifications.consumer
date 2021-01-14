package controllers

import akka.actor.ActorSystem
import javax.inject._
import model.{LocationModel}
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

  private val processIncomeLocationUpdates: LocationModel => Future[LookupResult] = location => {
    val promise = Promise[LookupResult]()

    actorSystem.scheduler.scheduleOnce(1.millisecond) {

      goToDb(location).onComplete(promise.complete)

    }(actorSystem.dispatcher)
    promise.future
  }


  def goToDb(income: LocationModel): Future[LookupResult] = {
    Future {
      val models: Seq[LocationModel] = new Repository().findByClientId(income.clientId).filter((location: LocationModel) => check(location, income))
      LookupResult(models, income.clientId)
    }
  }

  def check(fromDb: LocationModel, fromMobile: LocationModel) = fromMobile.latitude == fromDb.latitude && fromMobile.longitude == fromDb.longitude

  def placeToQueue(result: LookupResult): Unit = println(s"Placing to queue: $result")
}
