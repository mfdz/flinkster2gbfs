package eu.quiqua.geojson.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import eu.quiqua.geojson.model.Feature
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

class FeatureJsonAdapter(objectJsonAdapter: JsonAdapter<Any>?) : JsonAdapter<Feature>() {
    companion object {
        private const val GEOMETRY_ATTRIBUTE = "geometry"
        private const val PROPERTIES_ATTRIBUTE = "properties"
        private const val TYPE_ATTRIBUTE = "type"
    }

    private val options: JsonReader.Options =
        JsonReader.Options.of(GEOMETRY_ATTRIBUTE, PROPERTIES_ATTRIBUTE, TYPE_ATTRIBUTE)
    private val pointDelegate = PointJsonAdapter()
    private val lineStringDelegate = LineStringJsonAdapter()
    private val polygonDelegate = PolygonJsonAdapter()
    private val multiPointDelegate = MultiPointJsonAdapter()
    private val multiLineStringDelegate = MultiLineStringJsonAdapter()
    private val multiPolygonDelegate = MultiPolygonJsonAdapter()
    private val geometryCollectionDelegate = GeometryCollectionJsonAdapter()
    private val objectDelegate = objectJsonAdapter

    @FromJson
    override fun fromJson(reader: JsonReader): Feature {
        var type: Type? = null
        var geometry: Geometry? = null
        var properties: Map<*, *>? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> {
                    val rawJson = reader.readJsonValue() as Map<*, *>?
                    rawJson?.let {
                        when ((rawJson.getOrDefault(TYPE_ATTRIBUTE, "invalid") as String).toLowerCase()) {
                            "point" -> geometry = pointDelegate.fromJsonValue(rawJson)
                            "linestring" -> geometry = lineStringDelegate.fromJsonValue(rawJson)
                            "polygon" -> geometry = polygonDelegate.fromJsonValue(rawJson)
                            "multipoint" -> geometry = multiPointDelegate.fromJsonValue(rawJson)
                            "multilinestring" -> geometry = multiLineStringDelegate.fromJsonValue(rawJson)
                            "multipolygon" -> geometry = multiPolygonDelegate.fromJsonValue(rawJson)
                            "geometrycollection" -> geometry = geometryCollectionDelegate.fromJsonValue(rawJson)
                        }
                    }
                }
                1 -> properties = reader.readJsonValue() as Map<*, *>?
                2 -> type = Type.convertFromString(reader.nextString())
                -1 -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        if (type == null) {
            throw JsonDataException("Required type is missing at ${reader.path}")
        }
        if (type !== Type.Feature) {
            throw JsonDataException("Required type is not a Feature at ${reader.path}")
        }
        val feature = Feature(geometry = geometry, properties = properties)
        val validationResult = feature.validate()
        return when (validationResult) {
            is ValidationResult.Ok, is ValidationResult.Warning -> feature
            else -> throw JsonDataException(validationResult.reason)
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Feature?) {
        if (value == null) {
            throw JsonDataException("Feature was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.serializeNulls = true
        writer.beginObject()
        writer.name(GEOMETRY_ATTRIBUTE)
        value.geometry?.let {
            when (it.type) {
                Type.Point -> pointDelegate.toJson(writer, it as Point)
                Type.LineString -> lineStringDelegate.toJson(writer, it as LineString)
                Type.Polygon -> polygonDelegate.toJson(writer, it as Polygon)
                Type.MultiPoint -> multiPointDelegate.toJson(writer, it as MultiPoint)
                Type.MultiLineString -> multiLineStringDelegate.toJson(writer, it as MultiLineString)
                Type.MultiPolygon -> multiPolygonDelegate.toJson(writer, it as MultiPolygon)
                Type.GeometryCollection -> geometryCollectionDelegate.toJson(writer, it as GeometryCollection)
                else -> throw JsonDataException("Unable to serialize object of type ${it.type}.")
            }
        } ?: writer.nullValue()

        writer.name(PROPERTIES_ATTRIBUTE)
        objectDelegate?.let { delegate ->
            value.properties?.let { properties ->
                writer.beginObject()
                properties.forEach {
                    writer.name(it.key as String)
                    delegate.toJson(writer, it.value)
                }
                writer.endObject()
            }
        } ?: writer.nullValue()

        writer.name(TYPE_ATTRIBUTE)
        writer.value(Type.convertToString(value.type))
        writer.endObject()
    }
}
