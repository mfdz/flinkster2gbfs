package de.mfdz.flinkster2gbfs

import de.mfdz.flinkster2gbfs.flinkster.FlinksterService
import de.mfdz.flinkster2gbfs.flinkster.Model
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import de.mfdz.flinkster2gbfs.gbfs.*
import de.mfdz.flinkster2gbfs.json.TimeZoneAdapter
import de.mfdz.flinkster2gbfs.json.URLAdapter
import java.io.File
import java.net.URL
import java.util.*

class FlinksterProvider(val provider: String,
                        val systemInformation: SystemInformation,
                        val providerNetwork: Int = 2) {
}

fun createRegioRadProvider(): FlinksterProvider {
    return FlinksterProvider("regiorad_stuttgart", SystemInformation(
            "de.mfdz.flinkster.cab.regiorad_stuttgart",
            "DE", "RegioRadStuttgart", operator = "DB Call a Bike",
            timezone = TimeZone.getTimeZone("CET"),
            url = URL("https://www.callabike-interaktiv.de/de/staedte/stuttgart")))
}

class Flinkster2GBFS(private val token: String) {
    private val out = "."

    private val flinksterService = FlinksterService(token)

    private fun requestStations(flinksterProvider: FlinksterProvider): List<Model.Area> {
        return flinksterService.getAreas(flinksterProvider.providerNetwork,
                flinksterProvider.provider, "station")
    }

    private fun requestBookingProposals(flinksterProvider: FlinksterProvider,
                                        stations: List<Model.Area>): Map<String, Map<String, Model.RentalObject>> {
        return flinksterService.getBookingProposals(flinksterProvider.providerNetwork, stations)
    }

    fun generateGbfs(flinksterProvider: FlinksterProvider) {
        val stations = requestStations(flinksterProvider)
        val gbfsStations = convertToGbfsStationInfo(stations)
        val bookingProposals = requestBookingProposals(flinksterProvider, stations)
        val stationsStatus = convertToStationStatus(bookingProposals)
        GBFSWriter().writeGbfs(out, flinksterProvider.systemInformation, gbfsStations, stationsStatus)
    }

    private fun convertToGbfsStation(station: Model.Area): Station {
        return Station(station_id = station.uid,
                name = station.name,
                address = station.address.street,
                post_code = station.address.zipCode,
                // city = station.address.city //TODO there is no city/district in GBFS, extension needed?
                lat = station.lat,
                lon = station.lon
        )
    }

    private fun convertToStationStatus(bookingProposals: Map<String, Map<String, Model.RentalObject>>): StationsStatus {
        val stationsStatus = bookingProposals.map { convertToStationInfo(it) }
        return StationsStatus(stationsStatus)
    }

    private fun convertToStationInfo(it: Map.Entry<String, Map<String, Model.RentalObject>>): StationStatus {
        return StationStatus(
                station_id = it.key,
                // TODO bikes and pedelecs should become two publications, so we need to filter here
                num_bikes_available = it.value.size,
                // TODO Flinkster doesn't publish number of available docks, we shouls ask for this...
                num_docks_available = 99,
                // TODO better set explicitly to time of first request
                last_reported = Date().time
        )
    }

    private fun convertToGbfsStationInfo(stations: List<Model.Area>): StationInformation {
        val gbfsStations = stations.map { convertToGbfsStation(it) }
        return StationInformation(gbfsStations)
    }
}

fun main(args: Array<String>) {
    Flinkster2GBFS(System.getenv("FLINKSTER_TOKEN")).generateGbfs(createRegioRadProvider())
}
