package BACKEND.DATA.Extra

data class ExtraBus(
    val tripId:String,
    val lineId:String,
    val blockId:String,
    val vehicleId:String,
    val activeTrip:Boolean,
    val time: ExtraBusTime,
    val destination: ExtraBusDestination
    ) {
}