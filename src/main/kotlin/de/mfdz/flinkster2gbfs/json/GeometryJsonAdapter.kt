package de.mfdz.flinkster2gbfs.json

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
import eu.quiqua.geojson.moshi.*
import java.lang.NullPointerException

class GeometryJsonAdapter(objectJsonAdapter: JsonAdapter<Any>?) : JsonAdapter<Geometry>() {
    companion object {
        private const val COORDINATES_ATTRIBUTE = "coordinates"
        private const val TYPE_ATTRIBUTE = "type"
    }

    private val options: JsonReader.Options =
            JsonReader.Options.of(COORDINATES_ATTRIBUTE, TYPE_ATTRIBUTE)
    private val pointDelegate = PointJsonAdapter()
    private val lineStringDelegate = LineStringJsonAdapter()
    private val polygonDelegate = PolygonJsonAdapter()
    private val multiPointDelegate = MultiPointJsonAdapter()
    private val multiLineStringDelegate = MultiLineStringJsonAdapter()
    private val multiPolygonDelegate = MultiPolygonJsonAdapter()
    private val geometryCollectionDelegate = GeometryCollectionJsonAdapter()

    @FromJson
    override fun fromJson(reader: JsonReader): Geometry {
        var geometry: Geometry? = null
        val rawJson = reader.readJsonValue() as Map<*, *>
        when ((rawJson.getOrDefault(TYPE_ATTRIBUTE, "invalid") as String).toLowerCase()) {
            "point" -> geometry = pointDelegate.fromJsonValue(rawJson)
            "linestring" -> geometry = lineStringDelegate.fromJsonValue(rawJson)
            "polygon" -> geometry = polygonDelegate.fromJsonValue(rawJson)
            "multipoint" -> geometry = multiPointDelegate.fromJsonValue(rawJson)
            "multilinestring" -> geometry = multiLineStringDelegate.fromJsonValue(rawJson)
            "multipolygon" -> geometry = multiPolygonDelegate.fromJsonValue(rawJson)
            "geometrycollection" -> geometry = geometryCollectionDelegate.fromJsonValue(rawJson)
        }
        if (geometry == null) {
            throw JsonDataException("Required geometry is missing at ${reader.path}")
        }
        val validationResult = geometry.validate()
        return when (validationResult) {
            is ValidationResult.Ok, is ValidationResult.Warning -> geometry
            else -> throw JsonDataException(validationResult.reason)
        }
    }
    @ToJson
    override fun toJson(writer: JsonWriter, geometry: Geometry?) {
        if (geometry == null) {
            throw NullPointerException("Geometry was null! Wrap in .nullSafe() to write nullable values.")
        }
        when (geometry.type) {
            Type.Point -> pointDelegate.toJson(writer, geometry as Point)
            Type.LineString -> lineStringDelegate.toJson(writer, geometry as LineString)
            Type.Polygon -> polygonDelegate.toJson(writer, geometry as Polygon)
            Type.MultiPoint -> multiPointDelegate.toJson(writer, geometry as MultiPoint)
            Type.MultiLineString -> multiLineStringDelegate.toJson(writer, geometry as MultiLineString)
            Type.MultiPolygon -> multiPolygonDelegate.toJson(writer, geometry as MultiPolygon)
            Type.GeometryCollection -> geometryCollectionDelegate.toJson(writer, geometry as GeometryCollection)
            else -> throw JsonDataException("Unable to serialize object of type ${geometry.type}.")
        }
    }
}