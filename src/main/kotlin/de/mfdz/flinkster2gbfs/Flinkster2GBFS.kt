package de.mfdz.flinkster2gbfs

import de.mfdz.flinkster2gbfs.flinkster.FlinksterService
import de.mfdz.flinkster2gbfs.flinkster.Model
import de.mfdz.flinkster2gbfs.gbfs.*
import java.time.LocalDateTime
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
            url = URL("https://www.regioradstuttgart.de/de")))
}

class Flinkster2GBFS(private val token: String, private val logLevel: String) {
    private val out = "."

    private val flinksterService = FlinksterService(token, logLevel)

    private fun requestStations(flinksterProvider: FlinksterProvider): List<Model.Area> {
        return flinksterService.getAreas(flinksterProvider.providerNetwork,
                flinksterProvider.provider, "station")
    }

    private fun requestBookingProposals(flinksterProvider: FlinksterProvider,
                                        stations: List<Model.Area>): Map<String, Map<String, Model.RentalObject>> {
        return flinksterService.getBookingProposals(flinksterProvider.providerNetwork, stations)
    }

    fun generateGbfs(flinksterProvider: FlinksterProvider) {
        val requestTime = Date().time / 1000 // convert to seconds
        val stations = requestStations(flinksterProvider)
        val gbfsStations = convertToGbfsStationInfo(stations)
        val bookingProposals = requestBookingProposals(flinksterProvider, stations)
        val stationsStatus = convertToStationStatus(bookingProposals, requestTime)
        val herrenbergStatus = stationsStatus.stations.find { it.station_id == "7bd6d9cb-509b-4378-83db-cebb44ee1f6f" }
        if(herrenbergStatus == null) {
            println("Herrenberg bike rental station not found in GBFS feed!")
        }
        if(herrenbergStatus?.num_bikes_available == 0) {
            println("Herrenberg bike rental station has 0 free bikes")
            println("Status: $herrenbergStatus")
            println("Booking proposals: $bookingProposals")
        }
        // TODO set ttl to a reasonable value
        GBFSWriter().writeGbfs(out, requestTime, 60000, flinksterProvider.systemInformation, gbfsStations, stationsStatus)
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

    private fun convertToStationStatus(bookingProposals: Map<String, Map<String, Model.RentalObject>>, requestTime: Long): StationsStatus {
        val stationsStatus = bookingProposals.map { convertToStationInfo(it, requestTime) }
        return StationsStatus(stationsStatus)
    }

    private fun convertToStationInfo(it: Map.Entry<String, Map<String, Model.RentalObject>>, requestTime: Long): StationStatus {
        return StationStatus(
                station_id = it.key,
                // TODO bikes and pedelecs should become two publications, so we need to filter here
                num_bikes_available = it.value.size,
                // TODO Flinkster doesn't publish number of available docks, we should ask for this...
                num_docks_available = 99,
                last_reported = requestTime
        )
    }

    private fun convertToGbfsStationInfo(stations: List<Model.Area>): StationInformation {
        val gbfsStations = stations.map { convertToGbfsStation(it) }
        return StationInformation(gbfsStations)
    }
}

fun main(args: Array<String>) {
    val shouldLoop : Boolean = (System.getenv("FLINKSTER_LOOP") ?: "false").toBoolean()
    do {
        Flinkster2GBFS(System.getenv("FLINKSTER_TOKEN"),    System.getenv("LOG_LEVEL")
                ?: "NONE").generateGbfs(createRegioRadProvider())
        val current = LocalDateTime.now()
        println("$current Updated GBFS feed.")
    } while (shouldLoop)
}
