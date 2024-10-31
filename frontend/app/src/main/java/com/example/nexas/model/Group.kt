package com.example.nexas.model

import android.graphics.Bitmap

data class Group(
    var id: String,
    var name: String,
    var avatar: Bitmap?,
    var location: String,
    var description: String,
    var membersLimit: Int,
    var members: List<UserProfile>?,
    var messages: List<Message>?
)