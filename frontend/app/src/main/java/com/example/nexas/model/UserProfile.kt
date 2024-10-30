package com.example.nexas.model

import android.graphics.Bitmap

data class UserProfile(
    val uname: String,
    val fname: String,
    val lname: String,
    val email: String,
    val location: String,
    val description: String?,
    val avatar: Bitmap?,
    val background: Bitmap?,
    val age: Int,
    val hashedPassword: String,
    val id: String? = null // MongoDB id
) {
    // Utility method to convert String to ObjectId
    fun toObjectId(): org.bson.types.ObjectId? {
        return id?.let { org.bson.types.ObjectId(it) }
    }
}