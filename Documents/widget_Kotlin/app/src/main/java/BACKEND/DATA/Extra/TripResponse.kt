package BACKEND.DATA.Extra

data class TripResponse(
    val nextStop: Int,
    val delay: Long,
    val trip: Trip
)

data class Trip(
    val id: String,
    val blockId: String,
    val lineId: String,
    val shape: String,
    val destination: Destination,
    val stops: List<Stop>
)

data class Destination(
    val bg: String,
    val en: String
)

data class Stop(
    val id: String,
    val scheduled: Long
)