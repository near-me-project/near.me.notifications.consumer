package shared

object GpsLocationUtils {
  private val equatorialEarthRadius = 6378.1370D
  private val degreeToRadian: Double = Math.PI / 180D

  def isCloseEnough(x1: Double, y1: Double, x2: Double, y2: Double, radius: Double): Boolean = haversineFunctionInMeters(x1, y1, x2, y2) <= radius

  def getDistanceBetween(x1: Double, y1: Double, x2: Double, y2: Double): Double = haversineFunctionInMeters(x1, y1, x2, y2)

  private def haversineFunctionInMeters(lat1: Double, long1: Double, lat2: Double, long2: Double): Double =
    1000D * haversineFunctionInKM(lat1, long1, lat2, long2)

  private def haversineFunctionInKM(lat1: Double, long1: Double, lat2: Double, long2: Double): Double = {
    val degreesLong: Double = (long2 - long1) * degreeToRadian
    val degreesLat: Double = (lat2 - lat1) * degreeToRadian
    val a: Double = Math.pow(Math.sin(degreesLat / 2D), 2D) + Math.cos(lat1 * degreeToRadian) * Math.cos(lat2 * degreeToRadian) * Math.pow(Math.sin(degreesLong / 2D), 2D)
    val c: Double = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a))
    equatorialEarthRadius * c
  }
}
