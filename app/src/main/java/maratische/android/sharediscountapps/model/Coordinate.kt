package maratische.android.sharediscountapps.model

data class Coordinate(val lat: Double, val lon: Double, val time: Long)

// Радиус Земли в километрах
private const val EARTH_RADIUS = 6371.0

fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    // Преобразование координат в радианы
    val lat1Rad = Math.toRadians(lat1)
    val lon1Rad = Math.toRadians(lon1)
    val lat2Rad = Math.toRadians(lat2)
    val lon2Rad = Math.toRadians(lon2)

    // Разница координат
    val dlat = lat2Rad - lat1Rad
    val dlon = lon2Rad - lon1Rad

    // Вычисление расстояния с использованием формулы гаверсинуса
    val a = Math.pow(
        Math.sin(dlat / 2),
        2.0
    ) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(
        Math.sin(
            dlon / 2
        ), 2.0
    )
    val c =
        2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return EARTH_RADIUS * c
}

fun calculateTotalDistance(coordinates: List<Coordinate>): Double {
    var totalDistance = 0.0
    for (i in 0 until coordinates.size - 1) {
        val (lat, lon) = coordinates[i]
        val (lat1, lon1) = coordinates[i + 1]
        totalDistance += calculateDistance(lat, lon, lat1, lon1)
    }
    return totalDistance
}
