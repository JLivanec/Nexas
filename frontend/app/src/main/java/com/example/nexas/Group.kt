package com.example.nexas

import android.graphics.Bitmap

data class Group(
    var id: String,
    var name: String,
    var avatar: Bitmap?,
    var location: String,
    var description: String,
    var membersLimit: Int,
    var members: List<Member>,
    var messages: List<Message>
)