package com.example.nexas.model

import android.graphics.Bitmap

data class Group(
    var id: String,
    var name: String,
    var avatar: String,
    var location: String,
    var description: String,
    var membersLimit: Int,
    var members: List<Profile>?,
    var messages: List<Message>?
)