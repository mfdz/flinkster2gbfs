package eu.quiqua.geojson.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import eu.quiqua.geojson.model.Type
import eu.quiqua.geojson.model.geometry.MultiPoint
import eu.quiqua.geojson.model.geometry.Position
import eu.quiqua.geojson.model.geometry.ValidationResult
import java.lang.NullPointerException

class MultiPointJsonAdapter : JsonAdapter<MultiPoint>() {
    companion object {
        private const val COORDINATES_ATTRIBUTE = "coordinates"
        private const val TYPE_ATTRIBUTE = "type"
    }

    private val options: JsonReader.Options = JsonReader.Options.of(COORDINATES_ATTRIBUTE, TYPE_ATTRIBUTE)
    private val positionJsonAdapter = PositionJsonAdapter()

    @FromJson
    override fun fromJson(reader: JsonReader): MultiPoint {
        var type: Type? = null
        var coordinates: List<Position>? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> {
                    coordinates = mutableListOf()
                    reader.beginArray()
                    while (reader.hasNext()) {
                        coordinates.add(positionJsonAdapter.fromJson(reader))
                    }
                    reader.endArray()
                }
                1 -> type = Type.convertFromString(reader.nextString())
                -1 -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        if (coordinates == null) {
            throw JsonDataException("Required coordinates are missing at ${reader.path}")
        }
        if (type == null) {
            throw JsonDataException("Required type is missing at ${reader.path}")
        }
        if (type !== Type.MultiPoint) {
            throw JsonDataException("Required type is not a MultiPoint at ${reader.path}")
        }
        val multiPoint = MultiPoint(coordinates)
        val validationResult = multiPoint.validate()
        return when (validationResult) {
            is ValidationResult.Ok -> multiPoint
            else -> throw JsonDataException(validationResult.reason)
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: MultiPoint?) {
        if (value == null) {
            throw NullPointerException("MultiPoint was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name(MultiPointJsonAdapter.COORDINATES_ATTRIBUTE)
        writer.beginArray()
        value.coordinates.forEach {
            positionJsonAdapter.toJson(writer, it)
        }
        writer.endArray()

        writer.name(MultiPointJsonAdapter.TYPE_ATTRIBUTE)
        writer.value(Type.convertToString(value.type))
        writer.endObject()
    }
}
