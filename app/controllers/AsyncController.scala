package controllers

import akka.actor.ActorSystem
import javax.inject._
import model.{Location, LocationModel}
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class AsyncController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def message(clientId: String, latitude: String, longitude: String) = Action.async {
    Future {
      eventualEventualResult(LocationModel(clientId, latitude, longitude)).foreach(res => res.map(r => placeToQueue(r)))
      Ok
    }
  }

  private val eventualEventualResult: LocationModel => Future[Option[LookupResult]] = location => {
    val promise: Promise[Option[LookupResult]] = Promise[Option[LookupResult]]()
    actorSystem.scheduler.scheduleOnce(10.seconds) {
      promise.success(Option(LookupResult(List(Location("1", "2")), "1")))
    }(actorSystem.dispatcher)
    promise.future
  }

  def placeToQueue(result: LookupResult): Unit = println(s"Result has been processed: $result")
}
