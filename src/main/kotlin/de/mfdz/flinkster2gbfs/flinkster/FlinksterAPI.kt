package de.mfdz.flinkster2gbfs.flinkster

import com.squareup.moshi.Json
import eu.quiqua.geojson.model.geometry.Geometry
import eu.quiqua.geojson.model.geometry.Point
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URL
import java.util.*

object Model {
    data class AreasResult(val limit: Int,
                           val size: Int,
                           val offset: Int,
                           val href: String,
                           val items: List<Area>)

    data class Area(val uid: String,
                    val href: URL,
                    val name: String,
                    val providerAreaId: Int,
                    val type: String,
                    val address: Address,
                    val geometry: AreaGeometry
    ) {
        val lat: Double
                get() = (geometry.centroid?:(geometry.position as Point)).latitude
        val lon: Double
            get() = (geometry.centroid?:(geometry.position as Point)).longitude
    }

    data class AreaGeometry(val position: Geometry,
                            val centroid: Point?)

    data class Address(val street: String,
                       val number: String,
                       val zipCode: String,
                       val city: String,
                       val district: String,
                       val isoCountryCode: String)

    data class BookingProposalResult(val limit: Int,
                                     val size: Int,
                                     val offset: Int,
                                     val items: List<BookingProposal>)

    data class BookingProposal(
            val rentalObject: RentalObject,
            val area: Reference,
            val price: Reference
    )

    data class Reference (
        val href: URL
    )

    data class RentalObject(
            val href: URL,
            val uid: String,
            val name: String,
            val description: String,
            val providerRentalObjectId: Int,
            val rentalModel: RentalMode,
            val type: RentalObjectType,
            val provider: Reference,
            val category: Reference,
            val attributes: RentalObjectAttributes
    )

    enum class RentalMode {
        @Json(name="stationbased") STATION_BASED,
        @Json(name="parkingarea") PARIKING_AREA,
        @Json(name="freefloating") FREEFLOATING,
        @Json(name="freefloatingWithStation") FREEFLOATING_WITH_STATION}

    enum class RentalObjectType {
        @Json(name="bike")BIKE,
        @Json(name="pedelec")PEDELEC,
        @Json(name="vehicle")VEHICLE,
        @Json(name="vehiclepool")VEHICLE_POOL
    }

    data class RentalObjectAttributes (
            val licenseplate: String
    )
}

interface FlinksterAPI {

    @GET("areas")
    fun getAreas(@Query("providernetwork") providernetwork: Int,
                 @Query("limit") limit: Int? = null,
                 @Query("offset") offset: Int? = null,
                 @Query("provider") provider: String? = null,
                 @Query("type") type: String? = null): Call<Model.AreasResult>
    // TODO request infos concerning docks?

    @GET("bookingproposals")
    fun getBookingProposals(@Query("providernetwork") providernetwork: Int,
                            @Query("lat") lat: Double,
                            @Query("lon") lon: Double,
                            @Query("limit") limit: Int? = null,
                            @Query("offset") offset: Int? = null,
                            @Query("radius") radius: Int? = null,
                            @Query("expand") expand: String? = null,
                            @Query("begin") begin: Date? = null,
                            @Query("end") end: Date? = null): Call<Model.BookingProposalResult>

}