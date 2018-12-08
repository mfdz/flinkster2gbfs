package de.mfdz.flinkster2gbfs.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

class TimeZoneAdapter {
    @FromJson
    fun fromJson(string:String): TimeZone {
        return TimeZone.getTimeZone(string)
    }

    @ToJson
    fun toJson(timeZone: TimeZone):String {
        return timeZone.id
    }
}