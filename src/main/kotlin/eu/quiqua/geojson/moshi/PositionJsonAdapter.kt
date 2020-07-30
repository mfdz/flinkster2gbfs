package eu.quiqua.geojson.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import eu.quiqua.geojson.model.geometry.Position
import eu.quiqua.geojson.model.geometry.ValidationResult
import java.lang.NullPointerException

class PositionJsonAdapter : JsonAdapter<Position>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Position {
        val positions = mutableListOf<Double>()
        reader.beginArray()
        while (reader.hasNext()) {
            positions.add(reader.nextDouble())
        }
        reader.endArray()

        if (positions.size < 2) {
            throw JsonDataException("Required positions are missing at ${reader.path}")
        }

        val longitude = positions.elementAt(0)
        val latitude = positions.elementAt(1)
        val altitude = positions.elementAtOrNull(2)
        val position = Position(
            longitude = longitude,
            latitude = latitude,
            altitude = altitude
        )
        val validationResult = position.validate()
        return when (validationResult) {
            is ValidationResult.Ok -> position
            else -> throw JsonDataException(validationResult.reason)
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Position?) {
        if (value == null) {
            throw NullPointerException("Position was null! Wrap in .nullSafe() to write nullable values.")
        }

        writer.beginArray()
        writer.value(value.longitude)
        writer.value(value.latitude)
        if (value.hasAltitude) {
            writer.value(value.altitude)
        }
        writer.endArray()
    }
}
