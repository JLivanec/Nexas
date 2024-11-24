package com.example.nexas.model

import com.google.firebase.firestore.GeoPoint

data class Profile(
    var id: String,
    var username: String,
    var firstName: String,
    var lastName: String,
    var location: GeoPoint,
    var description: String,
    var avatar: String,
    var background: String,
    var age: Int
)