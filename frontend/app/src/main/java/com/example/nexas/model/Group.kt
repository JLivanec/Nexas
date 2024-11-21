package com.example.nexas.model

import com.google.firebase.firestore.GeoPoint

data class Group(
    var id: String,
    var name: String,
    var avatar: String,
    var location: GeoPoint,
    var description: String,
    var membersLimit: Int,
    var members: List<Profile>?,
    var messages: List<Message>?
)