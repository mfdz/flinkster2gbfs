package de.mfdz.flinkster2gbfs.flinkster

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import de.mfdz.flinkster2gbfs.json.GeometryJsonAdapter
import de.mfdz.flinkster2gbfs.json.URLAdapter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

class FlinksterService(val token:String, val level:String="NONE", val apiURL:String="https://api.deutschebahn.com/flinkster-api-ng/v1/"){
    val MAX_LIMIT_GET_AREAS = 100
    val MAX_LIMIT_GET_BOOKING_PROPOSALS = 100
    val MAX_RADIUS_GET_BOOKING_PROPOSALS = 10000

    var flinksterAPI:FlinksterAPI

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(level))
        val httpClient = OkHttpClient.Builder()
                .addInterceptor(AddHeaderInterceptor("Authorization", "Bearer "+token))
                .addInterceptor(AddHeaderInterceptor("Accept", "application/json"))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS) //
                .build()

        val moshi = Moshi.Builder()
                .add(URLAdapter())
                .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                .add(GeometryJsonAdapter(null))
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(apiURL)
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        flinksterAPI = retrofit.create(FlinksterAPI::class.java)
    }

    fun getAreas(
        providernetwork: Int,
        provider: String? = null,
        type: String? = null): List<Model.Area> {

        val areas = ArrayList<Model.Area>()
        var offset = 0
        do {
            val result = flinksterAPI.getAreas(providernetwork,
                    MAX_LIMIT_GET_AREAS, offset, provider, type).execute()
            val body = result.body()
            if (body == null) {
                print(result.errorBody())
                if (result.code()==500) {
                    println(result.raw())
                    println("Sleeping for 2s")
                    Thread.sleep(2000)
                } else {
                    throw IOException(result.message())
                }
            } else {
                areas.addAll(body!!.items)
                offset += MAX_LIMIT_GET_AREAS
            }
        } while (body == null || offset < body!!.size)

        return areas
    }

    private fun getBookingProposals(providerNetwork: Int, currentStation: Model.Area): List<Model.BookingProposal> {
        val proposals = ArrayList<Model.BookingProposal>()
        var offset = 0
        var totalSize = 0;

        do {
            try {
                val result = flinksterAPI.getBookingProposals(providerNetwork,
                        currentStation.lat, currentStation.lon, MAX_LIMIT_GET_BOOKING_PROPOSALS,
                        offset, MAX_RADIUS_GET_BOOKING_PROPOSALS, "rentalobject").execute()
            val body = result.body()
            if (body == null) {
                // TODO wait and retry if rate exceeded
                print(result.errorBody())
                if (result.code() == 500) {
                    println(result.raw())
                    val sleepSecs = 15L
                    println("Sleeping for ${sleepSecs}s")
                    Thread.sleep(sleepSecs * 1000)
                } else {
                    throw IOException(result.message())
                }
            } else {
                proposals.addAll(body.items)
                offset += MAX_LIMIT_GET_BOOKING_PROPOSALS
                totalSize = body.size
            }
            } catch (e: JsonDataException){
                print(e)
            }

        } while (offset < totalSize)
        return proposals
    }

    fun getBookingProposals(providerNetwork: Int, stations: List<Model.Area>): HashMap<String, MutableMap<String, Model.RentalObject>> {
        val proposalsPerStation = HashMap<String, MutableMap<String, Model.RentalObject>>()
        // put stations in hashSet stationsToRetrieve
        val stationsToRetrieve = stations.map{it.uid to it}.toMap().toMutableMap()
        while (!stationsToRetrieve.isEmpty()) {
            val currentStation = stationsToRetrieve.values.random()
            val currentStationUid = currentStation.uid

            // request all booking proposals around this coord
            val proposals = getBookingProposals(providerNetwork, currentStation)

            for (proposal in proposals) {
                val stationUid = proposal.area.href.toString().substringAfterLast("/")
                if (proposalsPerStation.containsKey(stationUid)){
                    proposalsPerStation[stationUid]!![proposal.rentalObject.uid] = proposal.rentalObject
                } else {
                    proposalsPerStation[stationUid] = mutableMapOf(proposal.rentalObject.uid to proposal.rentalObject)
                }
                // if any proposal was found for this station, we assume all will be found in this run
                stationsToRetrieve.remove(stationUid)
            }
            // if no rentalObject was found for this station, we add an empty collection to reflect this
            if (!proposalsPerStation.containsKey(currentStationUid)){
                println("Proposals did not contain station id ${currentStation}")
                proposalsPerStation[currentStationUid] = emptyMap<String, Model.RentalObject>().toMutableMap()
            }
            // remove the current station, even if no match was found
            stationsToRetrieve.remove(currentStationUid)
        }
        return proposalsPerStation
    }
}

class AddHeaderInterceptor(private val name:String, private val value:String): Interceptor {
    /**
     * Interceptor class which adds header for every request
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request?.newBuilder()
                ?.addHeader(name, value)
                ?.build()
        return chain.proceed(request)
    }
}
