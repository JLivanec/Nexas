package com.example.nexas.model

data class UserProfile(
    val userID: String,
    val uname: String,
    val fname: String,
    val lname: String,
    val email: String,
    val hashedPassword: String,
    val id: String? = null // Optional field for MongoDB id
) {
    // Utility method to convert String to ObjectId
    fun toObjectId(): org.bson.types.ObjectId? {
        return id?.let { org.bson.types.ObjectId(it) }
    }
}
