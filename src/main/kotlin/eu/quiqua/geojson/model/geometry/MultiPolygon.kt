package eu.quiqua.geojson.model.geometry

import eu.quiqua.geojson.model.Type

data class MultiPolygon(val coordinates: List<List<List<Position>>>) : Geometry {
    override val type: Type
        get() = Type.MultiPolygon

    override fun validate(): ValidationResult = GeometryValidation.isMultiPolygon(coordinates)
}
