package eu.quiqua.geojson.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import eu.quiqua.geojson.model.Feature
import java.lang.reflect.Type

internal class FeatureJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type?, annotations: MutableSet<out Annotation>?, moshi: Moshi?): JsonAdapter<*>? {
        if (type != null && type == Feature::class.java) {
            val objectAdapter = moshi?.adapter<Any>(Object::class.java)
            return FeatureJsonAdapter(objectAdapter)
        }
        return null
    }
}
