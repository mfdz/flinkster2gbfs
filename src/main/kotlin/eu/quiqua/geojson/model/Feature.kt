package eu.quiqua.geojson.model

import eu.quiqua.geojson.model.geometry.Geometry
import eu.quiqua.geojson.model.geometry.ValidationResult

data class Feature(val geometry: Geometry? = null, val properties: Map<*, *>? = null) : GeoJson {
    override val type: Type
        get() = Type.Feature

    override fun validate(): ValidationResult {
        return geometry?.validate() ?: ValidationResult.Ok()
    }
}
