package controllers

import akka.actor.ActorSystem
import com.mongodb.CursorType
import javax.inject._
import model.LocationModel
import org.mongodb.scala.bson.conversions
import org.mongodb.scala.{Document, FindObservable, SingleObservable, bson}
import play.api.mvc._
import repository.Repository

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

@Singleton
class AsyncController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def message(clientId: String, latitude: String, longitude: String) = Action.async {
    Future {
      eventualEventualResult(LocationModel(clientId, latitude, longitude)).foreach(res => res.map(r => placeToQueue(r)))
      Ok
    }
  }

  private val eventualEventualResult: LocationModel => Future[Option[LookupResult]] = location => {
    val promise = Promise[Option[LookupResult]]()

    actorSystem.scheduler.scheduleOnce(1.seconds) {

      goToDb(location).onComplete {
        case Success(res) => promise.complete(Try(Option(res)))
        case Failure(e) => promise.complete(Try(None))
      }

    }(actorSystem.dispatcher)
    promise.future
  }


  def goToDb(locationModel: LocationModel): Future[LookupResult] = {

    val res: FindObservable[Document] = new Repository().findByClientId(locationModel.clientId)

  }

  def placeToQueue(result: LookupResult): Unit = ???
}
