package com.example.nexas.model

import java.time.Instant
import android.graphics.Bitmap

data class Message(
    var id: String,
    var senderID: String,
    var groupID: String,
    var videoImage: Bitmap?,
    var videoID: String,
    var timestamp: Instant = Instant.now() // defaulting to current time
)