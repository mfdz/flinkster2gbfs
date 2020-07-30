package eu.quiqua.geojson.extension

import eu.quiqua.geojson.model.geometry.ValidationResult

internal fun List<ValidationResult>.getFirstErrorOrOk(): ValidationResult {
    return with(filterNot { it is ValidationResult.Ok }) {
        if (isEmpty()) ValidationResult.Ok() else first()
    }
}
