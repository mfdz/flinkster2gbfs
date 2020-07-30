package eu.quiqua.geojson.model.geometry

sealed class ValidationResult(open val reason: String?) {
    class Ok(reason: String? = null) : ValidationResult(reason)

    sealed class Error(override val reason: String) : ValidationResult(reason) {
        data class OutOfRange(override val reason: String) : Error(reason)
        data class TooFewElements(override val reason: String) : Error(reason)
        data class NoLinearRing(override val reason: String) : Error(reason)
        data class IncompatibleCoordinateDimensions(override val reason: String) : Error(reason)
    }

    sealed class Warning(override val reason: String) : ValidationResult(reason) {
        data class AvoidNestedGeometryCollections(override val reason: String) : Warning(reason)
        data class AvoidSingleGeometry(override val reason: String) : Warning(reason)
    }
}
