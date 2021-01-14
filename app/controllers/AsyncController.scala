package controllers

import akka.actor.ActorSystem
import javax.inject._
import model.{LocationModel}
import play.api.mvc._
import repository.Repository

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

@Singleton
class AsyncController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def message(clientId: String, latitude: String, longitude: String) = Action.async {
    Future {
      processIncomeLocationUpdates(LocationModel(clientId, latitude, longitude)).foreach(res => res.map(r => placeToQueue(r)))
      Ok
    }
  }

  private val processIncomeLocationUpdates: LocationModel => Future[Option[LookupResult]] = location => {
    val promise = Promise[Option[LookupResult]]()

    actorSystem.scheduler.scheduleOnce(1.millisecond) {

      goToDb(location).onComplete {
        case Success(res) => promise.complete(Try(Option(res)))
        case Failure(e) => promise.complete(Try(None))
      }

    }(actorSystem.dispatcher)
    promise.future
  }


  def goToDb(income: LocationModel): Future[LookupResult] = {

    val models: Seq[LocationModel] = new Repository().findByClientId(income.clientId)
      .filter((location: LocationModel) => check(location, income))

    Future(LookupResult(models, income.clientId))
  }

  def check(fromDb: LocationModel, fromMobile: LocationModel) = fromMobile.latitude == fromDb.latitude && fromMobile.longitude == fromDb.longitude

  def placeToQueue(result: LookupResult): Unit = println(s"Placing to queue: $result")
}
