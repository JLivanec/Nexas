package com.example.nexas.data

import kotlinx.serialization.Serializable

/*
    Serializable data class to store the latitude and longitude location of a user.
    Serializable makes it so JSON.encodeToString and Json.decodeFromString work on it
    to store in the db.
 */

@Serializable
data class CustomLocation(
    val latitude: Double,
    val longitude: Double
)

val stateAbbreviations = mapOf(
    "Alabama" to "AL",
    "Alaska" to "AK",
    "Arizona" to "AZ",
    "Arkansas" to "AR",
    "California" to "CA",
    "Colorado" to "CO",
    "Connecticut" to "CT",
    "Delaware" to "DE",
    "Florida" to "FL",
    "Georgia" to "GA",
    "Hawaii" to "HI",
    "Idaho" to "ID",
    "Illinois" to "IL",
    "Indiana" to "IN",
    "Iowa" to "IA",
    "Kansas" to "KS",
    "Kentucky" to "KY",
    "Louisiana" to "LA",
    "Maine" to "ME",
    "Maryland" to "MD",
    "Massachusetts" to "MA",
    "Michigan" to "MI",
    "Minnesota" to "MN",
    "Mississippi" to "MS",
    "Missouri" to "MO",
    "Montana" to "MT",
    "Nebraska" to "NE",
    "Nevada" to "NV",
    "New Hampshire" to "NH",
    "New Jersey" to "NJ",
    "New Mexico" to "NM",
    "New York" to "NY",
    "North Carolina" to "NC",
    "North Dakota" to "ND",
    "Ohio" to "OH",
    "Oklahoma" to "OK",
    "Oregon" to "OR",
    "Pennsylvania" to "PA",
    "Rhode Island" to "RI",
    "South Carolina" to "SC",
    "South Dakota" to "SD",
    "Tennessee" to "TN",
    "Texas" to "TX",
    "Utah" to "UT",
    "Vermont" to "VT",
    "Virginia" to "VA",
    "Washington" to "WA",
    "West Virginia" to "WV",
    "Wisconsin" to "WI",
    "Wyoming" to "WY"
)
