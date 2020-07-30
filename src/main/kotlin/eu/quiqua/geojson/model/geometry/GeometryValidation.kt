package eu.quiqua.geojson.model.geometry

import eu.quiqua.geojson.extension.getFirstErrorOrOk

object GeometryValidation {

    private const val MINIMUM_LINEAR_RING_COORDINATES = 4

    fun isPoint(coordinates: Position): ValidationResult = coordinates.validate()

    fun isMultiPoint(coordinates: List<Position>): ValidationResult {
        return when {
            coordinates.isEmpty() -> ValidationResult.Error.TooFewElements("No coordinates provided to create a MultiPoint")
            else -> {
                val validations = mutableListOf(
                    hasConsistentDimension(coordinates)
                )
                coordinates.forEach {
                    validations.add(isPoint(it))
                }
                validations.getFirstErrorOrOk()
            }
        }
    }

    fun isLineString(coordinates: List<Position>): ValidationResult {
        val validations = mutableListOf(
            hasAtLeastTwoCoordinates(coordinates),
            hasConsistentDimension(coordinates)
        )

        coordinates.forEach {
            validations.add(isPoint(it))
        }

        return validations.getFirstErrorOrOk()
    }

    fun isMultiLineString(coordinates: List<List<Position>>): ValidationResult {
        return when {
            coordinates.isEmpty() -> ValidationResult.Error.TooFewElements("No coordinates provided to create a MultiLineString")
            else -> {
                val validations = mutableListOf<ValidationResult>()
                coordinates.forEach {
                    validations.add(isLineString(it))
                }
                validations.getFirstErrorOrOk()
            }
        }
    }

    fun isPolygon(coordinates: List<List<Position>>): ValidationResult {
        return when {
            coordinates.isEmpty() -> ValidationResult.Error.TooFewElements("No coordinates provided to create a Polygon")
            else -> {
                val validations = mutableListOf<ValidationResult>()
                coordinates.forEach {
                    validations.add(isLinearRing(it))
                }
                validations.getFirstErrorOrOk()
            }
        }
    }

    fun isMultiPolygon(coordinates: List<List<List<Position>>>): ValidationResult {
        return when {
            coordinates.isEmpty() -> ValidationResult.Error.TooFewElements("No coordinates provided to create a MultiPolygon")
            else -> {
                val validations = mutableListOf<ValidationResult>()
                coordinates.forEach {
                    validations.add(isPolygon(it))
                }
                validations.getFirstErrorOrOk()
            }
        }
    }

    private fun hasAtLeastTwoCoordinates(coordinates: List<Position>): ValidationResult {
        return when (coordinates.count() < 2) {
            true -> ValidationResult.Error.TooFewElements("A LineString consists of at least two coordinate pairs")
            false -> ValidationResult.Ok()
        }
    }

    private fun hasConsistentDimension(coordinates: List<Position>): ValidationResult {
        val coordinatesWithAltitude = coordinates.filter { it.hasAltitude }
        val coordinatesWithoutAltitude = coordinates.filterNot { it.hasAltitude }
        return when (coordinatesWithAltitude.isNotEmpty() and coordinatesWithoutAltitude.isNotEmpty()) {
            true -> ValidationResult.Error.IncompatibleCoordinateDimensions("Coordinates consist of 2D and 3D geometries")
            false -> ValidationResult.Ok()
        }
    }

    private fun isLinearRing(coordinates: List<Position>): ValidationResult {
        val validation = isLineString(coordinates)
        return when (validation) {
            is ValidationResult.Ok -> with(coordinates) {
                if (count() >= MINIMUM_LINEAR_RING_COORDINATES && first() == last()) {
                    ValidationResult.Ok()
                } else {
                    ValidationResult.Error.NoLinearRing("The coordinates do not meet the LinearRing criteria")
                }
            }
            else -> validation
        }
    }
}
