package eu.quiqua.geojson.model

sealed class Type {
    object Point : Type()
    object LineString : Type()
    object Polygon : Type()
    object MultiPoint : Type()
    object MultiLineString : Type()
    object MultiPolygon : Type()
    object GeometryCollection : Type()
    object Feature : Type()
    object FeatureCollection : Type()
    object Unknown : Type()

    companion object {
        fun convertFromString(value: String): Type {
            return when (value.toLowerCase()) {
                "point" -> Point
                "linestring" -> LineString
                "polygon" -> Polygon
                "multipoint" -> MultiPoint
                "multilinestring" -> MultiLineString
                "multipolygon" -> MultiPolygon
                "geometrycollection" -> GeometryCollection
                "feature" -> Feature
                "featurecollection" -> FeatureCollection
                else -> Unknown
            }
        }

        fun convertToString(value: Type): String {
            return when (value) {
                is Point -> "Point"
                is LineString -> "LineString"
                is Polygon -> "Polygon"
                is MultiPoint -> "MultiPoint"
                is MultiLineString -> "MultiLineString"
                is MultiPolygon -> "MultiPolygon"
                is GeometryCollection -> "GeometryCollection"
                is Feature -> "Feature"
                is FeatureCollection -> "FeatureCollection"
                else -> "Unknown"
            }
        }
    }
}
