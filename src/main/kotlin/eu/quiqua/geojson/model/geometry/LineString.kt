package eu.quiqua.geojson.model.geometry

import eu.quiqua.geojson.model.Type

data class LineString(val coordinates: List<Position>) : Geometry {
    override val type: Type
        get() = Type.LineString

    override fun validate(): ValidationResult = GeometryValidation.isLineString(coordinates)
}
