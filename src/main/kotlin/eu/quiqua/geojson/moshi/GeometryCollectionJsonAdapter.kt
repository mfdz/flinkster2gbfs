package eu.quiqua.geojson.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import eu.quiqua.geojson.model.Type
import eu.quiqua.geojson.model.geometry.Geometry
import eu.quiqua.geojson.model.geometry.GeometryCollection
import eu.quiqua.geojson.model.geometry.LineString
import eu.quiqua.geojson.model.geometry.MultiLineString
import eu.quiqua.geojson.model.geometry.MultiPoint
import eu.quiqua.geojson.model.geometry.MultiPolygon
import eu.quiqua.geojson.model.geometry.Point
import eu.quiqua.geojson.model.geometry.Polygon
import eu.quiqua.geojson.model.geometry.ValidationResult
import java.lang.NullPointerException

class GeometryCollectionJsonAdapter : JsonAdapter<GeometryCollection>() {
    companion object {
        private const val GEOMETRIES_ATTRIBUTE = "geometries"
        private const val TYPE_ATTRIBUTE = "type"
    }

    private val options: JsonReader.Options = JsonReader.Options.of(GEOMETRIES_ATTRIBUTE, TYPE_ATTRIBUTE)
    private val pointDelegate = PointJsonAdapter()
    private val lineStringDelegate = LineStringJsonAdapter()
    private val polygonDelegate = PolygonJsonAdapter()
    private val multiPointDelegate = MultiPointJsonAdapter()
    private val multiLineStringDelegate = MultiLineStringJsonAdapter()
    private val multiPolygonDelegate = MultiPolygonJsonAdapter()

    @FromJson
    override fun fromJson(reader: JsonReader): GeometryCollection {
        var type: Type? = null
        var geometries: List<Geometry>? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> {
                    geometries = mutableListOf()
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val value = reader.readJsonValue() as Map<String, String>
                        when (value.getOrDefault(TYPE_ATTRIBUTE, "invalid").toLowerCase()) {
                            "point" -> pointDelegate.fromJsonValue(value)?.let { geometries.add(it) }
                            "linestring" -> lineStringDelegate.fromJsonValue(value)?.let { geometries.add(it) }
                            "polygon" -> polygonDelegate.fromJsonValue(value)?.let { geometries.add(it) }
                            "multipoint" -> multiPointDelegate.fromJsonValue(value)?.let { geometries.add(it) }
                            "multilinestring" -> multiLineStringDelegate.fromJsonValue(value)?.let { geometries.add(it) }
                            "multipolygon" -> multiPolygonDelegate.fromJsonValue(value)?.let { geometries.add(it) }
                        }
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

        if (geometries == null) {
            throw JsonDataException("Required geometries is missing at ${reader.path}")
        }

        if (type == null) {
            throw JsonDataException("Required type is missing at ${reader.path}")
        }
        if (type !== Type.GeometryCollection) {
            throw JsonDataException("Required type is not a GeometryCollection at ${reader.path}")
        }
        val geometryCollection = GeometryCollection(geometries)
        val validationResult = geometryCollection.validate()
        return when (validationResult) {
            is ValidationResult.Ok, is ValidationResult.Warning -> geometryCollection
            else -> throw JsonDataException(validationResult.reason)
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: GeometryCollection?) {
        if (value == null) {
            throw NullPointerException("GeometryCollection was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name(GEOMETRIES_ATTRIBUTE)
        writer.beginArray()
        value.geometries.forEach {
            when (it.type) {
                Type.Point -> pointDelegate.toJson(writer, it as Point)
                Type.LineString -> lineStringDelegate.toJson(writer, it as LineString)
                Type.Polygon -> polygonDelegate.toJson(writer, it as Polygon)
                Type.MultiPoint -> multiPointDelegate.toJson(writer, it as MultiPoint)
                Type.MultiLineString -> multiLineStringDelegate.toJson(writer, it as MultiLineString)
                Type.MultiPolygon -> multiPolygonDelegate.toJson(writer, it as MultiPolygon)
                else -> throw JsonDataException("Unable to serialize object of type ${it.type}.")
            }
        }
        writer.endArray()

        writer.name(TYPE_ATTRIBUTE)
        writer.value(Type.convertToString(value.type))
        writer.endObject()
    }
}
