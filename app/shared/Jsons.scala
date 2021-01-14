package shared

import model.{LocationModel, LookupResult}
import play.api.libs.json.{Json, Writes}

object Jsons {
  implicit val locationWrites = new Writes[LocationModel] {
    def writes(location: LocationModel) = Json.obj(
      "clientId" -> location.clientId,
      "latitude" -> location.latitude,
      "longitude" -> location.longitude
    )
  }

  implicit val lookupResultWrites = new Writes[LookupResult] {
    def writes(lookupResult: LookupResult) = Json.obj(
      "nearestPlaces" -> lookupResult.nearestPlaces,
      "clientId" -> lookupResult.clientId
    )
  }

  def toJson(res: LookupResult): String = Json.toJson(res).toString()

}
