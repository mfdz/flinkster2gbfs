package eu.quiqua.geojson.model.geometry

import eu.quiqua.geojson.model.Type

data class Point(val coordinates: Position) : Geometry {
    override val type: Type
        get() = Type.Point

    override fun validate(): ValidationResult = GeometryValidation.isPoint(coordinates)

    val latitude = coordinates.latitude
    val longitude = coordinates.longitude
    val altitude = coordinates.altitude
}
