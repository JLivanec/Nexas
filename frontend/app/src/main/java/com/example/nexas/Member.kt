package com.example.nexas

import android.graphics.Bitmap

data class Member(
    var id: String,
    var name: String,
    var avatar: Bitmap?,
    var location: String,
    var description: String,
    var background: Bitmap?
)