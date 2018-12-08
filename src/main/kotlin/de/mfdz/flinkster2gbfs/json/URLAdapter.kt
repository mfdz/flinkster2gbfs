package de.mfdz.flinkster2gbfs.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.net.URL

class URLAdapter {
    @FromJson
    fun fromJson(string:String): URL {
        return URL(string)
    }

    @ToJson
    fun toJson(url: URL):String {
        return url.toString()
    }
}