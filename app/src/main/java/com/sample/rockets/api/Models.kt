package com.sample.rockets.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Keep only interested data in model definitions. This would help parsers finish faster.
 */
data class LaunchResponse(
    @SerializedName("launches")
    @Expose
    var launches: List<Launch> = emptyList()
)

data class Launch(
    @SerializedName("id")
    @Expose
    var id: Int = 0,
    @SerializedName("name")
    @Expose
    var name: String = "",
    @SerializedName("windowstart")
    @Expose
    var windowstart: String? = null,
    @SerializedName("wsstamp")
    @Expose
    var wsstamp: Long = 0L,
    @SerializedName("location")
    @Expose
    var location: Location? = null,
    @SerializedName("rocket")
    @Expose
    var rocket: Rocket? = null,
    @SerializedName("missions")
    @Expose
    var missions: List<Mission> = emptyList()
)

data class Agency(
    @SerializedName("id")
    @Expose
    var id: Int = 0,
    @SerializedName("name")
    @Expose
    var name: String = "Unknown"
)

data class Location(
    @SerializedName("pads")
    @Expose
    var pads: List<Pad> = emptyList()
)

data class Mission(
    @SerializedName("id")
    @Expose
    var id: Int = 0,
    @SerializedName("name")
    @Expose
    var name: String = "Unknown",
    @SerializedName("description")
    @Expose
    var description: String = "No Description available",
    @SerializedName("agencies")
    @Expose
    var agencies: List<Agency> = emptyList()
)

data class Pad(
    @SerializedName("latitude")
    @Expose
    var latitude: Double = 0.0,
    @SerializedName("longitude")
    @Expose
    var longitude: Double = 0.0
)

data class Rocket(
    @SerializedName("name")
    @Expose
    var name: String = "Unknown"
)