package com.example.nexas.model

import com.google.firebase.Timestamp

data class Message(
    var id: String,
    var senderID: String,
    var videoImage: String,
    var video: String,
    var timestamp: Timestamp = Timestamp.now(),
    val likedBy: MutableList<String> = mutableListOf(),
    var pinned: Boolean
)