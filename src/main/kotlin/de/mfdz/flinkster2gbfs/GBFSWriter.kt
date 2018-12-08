package de.mfdz.flinkster2gbfs

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import de.mfdz.flinkster2gbfs.gbfs.StationInformation
import de.mfdz.flinkster2gbfs.gbfs.StationsStatus
import de.mfdz.flinkster2gbfs.gbfs.SystemInformation
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

    fun writeGbfs(out: String, systemInformation: SystemInformation, gbfsStations: StationInformation, stationsStatus: StationsStatus) {
        val jsonStationInformationAdapter = moshi.adapter<StationInformation>(StationInformation::class.java)
        val jsonStationStatusAdapter = moshi.adapter<StationsStatus>(StationsStatus::class.java)
        val jsonSystemInformationAdapter = moshi.adapter<SystemInformation>(SystemInformation::class.java)

        writeGbfsFile(out, "station_information.json", jsonStationInformationAdapter.toJson(gbfsStations))
        writeGbfsFile(out, "system_information.json", jsonSystemInformationAdapter.toJson(systemInformation))
        writeGbfsFile(out, "station_status.json", jsonStationStatusAdapter.toJson(stationsStatus))
    }

    private fun writeGbfsFile(out: String, fileName: String, json: String) {
        File(out + File.separator + fileName).writeText(json)
    }
}