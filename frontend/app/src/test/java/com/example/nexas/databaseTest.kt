package com.example.nexas
import com.example.nexas.data.MongoClientConnection
import com.example.nexas.model.UserProfile
import kotlinx.coroutines.runBlocking
import org.mindrot.jbcrypt.BCrypt
import android.graphics.BitmapFactory
import android.graphics.Bitmap

fun main() {
    runBlocking {
        val mongoClientConnection = MongoClientConnection()

        // Test data for cat profile
        val catUserProfile = UserProfile(
            uname = "coolest_cat",
            fname = "Cool",
            lname = "Cat",
            email = "coolcat@example.com",
            location = "Whiskerville",
            description = "The coolest cat in town!",
            avatar = null, // You can provide a Bitmap if needed
            background = null, // You can provide a Bitmap if needed
            age = 5, // Example age for the cat
            hashedPassword = BCrypt.hashpw("purrfect_password", BCrypt.gensalt())
        )

        // Test insertion of cat profile
        val insertResult = mongoClientConnection.insertUserProfile(catUserProfile)
        if (insertResult) {
            println("Cat user profile inserted successfully.")
        } else {
            println("Failed to insert cat user profile.")
        }

        // Test credential validation for the cat profile
        val isValid = mongoClientConnection.validateCredentials("coolest_cat", "purrfect_password")
        if (isValid) {
            println("Cat test credentials are valid.")
        } else {
            println("Cat test credentials are invalid.")
        }

        // Test invalid credential validation
        val isInvalid = mongoClientConnection.validateCredentials("coolest_cat", "wrongPassword")
        if (!isInvalid) {
            println("Invalid credentials for cat profile correctly identified.")
        } else {
            println("Error: Invalid credentials for cat profile were not identified.")
        }

        // Test user retrieval for the cat profile
        val retrievedCatProfile = mongoClientConnection.getUserProfileByUsername("coolest_cat")
        if (retrievedCatProfile != null) {
            println("Retrieved cat user profile: $retrievedCatProfile")
        } else {
            println("Cat user profile not found.")
        }

        // Close the MongoDB connection
        mongoClientConnection.close()
    }
}