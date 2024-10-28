package com.example.nexas

import android.graphics.Bitmap

data class Message(
    var id: String,
    var senderID: String,
    var videoImage: Bitmap?,
    var videoID: String,
)