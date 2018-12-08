package de.mfdz.flinkster2gbfs

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import de.mfdz.flinkster2gbfs.gbfs.*
import de.mfdz.flinkster2gbfs.json.TimeZoneAdapter
import de.mfdz.flinkster2gbfs.json.URLAdapter
import java.io.File
import java.util.*

class GBFSWriter() {

    val moshi = Moshi.Builder()
            .add(URLAdapter())
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .add(TimeZoneAdapter())
            .build()

    fun writeGbfs(out: String, last_updated: Long, ttl: Int, systemInformation: SystemInformation, gbfsStations: StationInformation, stationsStatus: StationsStatus) {
        val jsonGBFSMetaStationInformationAdapter = moshi.adapter<GBFSMetaStationInformation>(GBFSMetaStationInformation::class.java)
        val jsonGBFSMetaStationsStatusAdapter = moshi.adapter<GBFSMetaStationsStatus>(GBFSMetaStationsStatus::class.java)
        val jsonGBFSMetaSystemInformationAdapter = moshi.adapter<GBFSMetaSystemInformation>(GBFSMetaSystemInformation::class.java)

        writeGbfsFile(out, "station_information.json", jsonGBFSMetaStationInformationAdapter.toJson(GBFSMetaStationInformation(last_updated, ttl, gbfsStations)))
        writeGbfsFile(out, "system_information.json", jsonGBFSMetaSystemInformationAdapter.toJson(GBFSMetaSystemInformation(last_updated, ttl, systemInformation)))
        writeGbfsFile(out, "station_status.json", jsonGBFSMetaStationsStatusAdapter.toJson(GBFSMetaStationsStatus(last_updated, ttl, stationsStatus)))
}

    private fun writeGbfsFile(out: String, fileName: String, json: String) {
        File(out + File.separator + fileName).writeText(json)
    }
}