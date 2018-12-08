package de.mfdz.flinkster2gbfs.gbfs

import java.net.URL
import java.util.*

data class SystemInformation(val system_id: String,
                             val language: String,
                             val name: String,
                             val short_name: String? = null,
                             val operator: String? = null,
                             val url: URL? = null,
                             val purchase_url: URL? = null,
                             val start_date: java.util.Date? = null,
                             val phone_number: String? = null,
                             val email: String? = null,
                             val timezone: TimeZone,
                             val license_url: URL? = null)

data class StationsStatus(val stations: List<StationStatus>)

data class StationStatus(val station_id: String,
                         val num_bikes_available: Int,
                         val num_bikes_disabled: Int? = null,
                         val num_docks_available: Int,
                         val num_docks_disables: Int? = null,
                         val is_installed: Boolean = true,
                         val is_renting: Boolean = true,
                         val is_returning: Boolean = true,
                         val last_reported: Long)
// TODO POSIX timestamp representation?

data class StationInformation(val stations: List<Station>)

data class Station(val station_id: String,
                   val name: String,
                   val lat: Double,
                   val lon: Double,
                   val short_name: String? = null,
                   val address: String? = null,
                   val cross_street: String? = null,
                   val region_id: String? = null,
                   val post_code: String? = null,
                   val rental_methods: List<RentalMode>? = null,
                   val capacity: Int?=null)

enum class RentalMode{KEY, CREDITCARD, PAYPASS, APPLEPAY, ANDROIDPAY, TRANSITCARD, ACCOUNTNUMBER
,PHONE}

// TODO add optional and freefloating GBFS entities