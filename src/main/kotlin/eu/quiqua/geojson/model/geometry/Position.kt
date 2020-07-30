package eu.quiqua.geojson.model.geometry

data class Position(val longitude: Double, val latitude: Double, val altitude: Double? = null) {
    companion object {
        private const val minimumLatitude = -90
        private const val maximumLatitude = 90
        private const val minimumLongitude = -180
        private const val maximumLongitude = 180
        val latitudeBoundaries = minimumLatitude..maximumLatitude
        val longitudeBoundaries = minimumLongitude..maximumLongitude
    }

    fun validate(): ValidationResult {
        if (longitude !in longitudeBoundaries) {
            return ValidationResult.Error.OutOfRange("Longitude '$longitude' is out of range -180 to 180")
        }
        if (latitude !in latitudeBoundaries) {
            return ValidationResult.Error.OutOfRange("Latitude '$latitude' is out of range -90 to 90")
        }
        return ValidationResult.Ok()
    }

    val hasAltitude: Boolean get() = altitude != null
}
