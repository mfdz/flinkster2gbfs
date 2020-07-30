package eu.quiqua.geojson.model

import eu.quiqua.geojson.model.geometry.ValidationResult

interface GeoJson {
    val type: Type
    fun validate(): ValidationResult
}
