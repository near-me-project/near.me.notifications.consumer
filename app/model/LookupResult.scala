package model

case class LookupResult(var nearestPlaces: List[LocationModel], var clientId: String)
