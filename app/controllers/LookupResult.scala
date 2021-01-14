package controllers

import model.LocationModel

case class LookupResult(nearestPlaces: Seq[LocationModel], clientId: String)
