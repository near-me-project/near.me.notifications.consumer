package controllers

import model.Location

case class LookupResult(nearestPlaces: List[Location], clientId: String)
