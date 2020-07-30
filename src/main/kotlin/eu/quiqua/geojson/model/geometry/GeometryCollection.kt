package eu.quiqua.geojson.model.geometry

import eu.quiqua.geojson.extension.getFirstErrorOrOk
import eu.quiqua.geojson.model.Type

data class GeometryCollection(val geometries: List<Geometry>) : Geometry {
    override val type: Type
        get() = Type.GeometryCollection

    override fun validate(): ValidationResult {
        val validations = geometries.map {
            when (it.type) {
                is Type.GeometryCollection -> ValidationResult.Warning.AvoidNestedGeometryCollections("GeometryCollection should avoid nested geometry collections")
                else -> it.validate()
            }
        }.toMutableList()
        if (geometries.isNotEmpty() && geometries.size == 1) {
            validations.add(ValidationResult.Warning.AvoidSingleGeometry("GeometryCollection with a single geometry should be avoided in favor of single part or a single object of multi-part type"))
        }
        return validations.getFirstErrorOrOk()
    }
}
