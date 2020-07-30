package eu.quiqua.geojson.model.geometry

import eu.quiqua.geojson.model.Type

data class MultiPoint(val coordinates: List<Position>) : Geometry {
    override val type: Type
        get() = Type.MultiPoint

    override fun validate(): ValidationResult = GeometryValidation.isMultiPoint(coordinates)
}
