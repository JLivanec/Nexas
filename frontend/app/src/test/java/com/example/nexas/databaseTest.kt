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

        // Test data for user profile including additional fields
        val testUser = UserProfile(
            uname = "coolest_cat",
            fname = "Cool",
            lname = "Cat",
            email = "lowtemperaturefeline@example.com",
            location = "Feline City",
            description = "Just a cool cat living life.",
            avatar = null, // You can provide a Bitmap if needed
            background = null, // You can provide a Bitmap if needed
            age = 5, // Example age
            hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt())
        )

        // Test insertion
        val insertResult = mongoClientConnection.insertUserProfile(testUser)
        if (insertResult) {
            println("Test user profile inserted successfully.")
        } else {
            println("Failed to insert test user profile.")
        }

        // Test credential validation
        val isValid = mongoClientConnection.validateCredentials("coolest_cat", "password123")
        if (isValid) {
            println("Test credentials are valid.")
        } else {
            println("Test credentials are invalid.")
        }

        // Test invalid credential validation
        val isInvalid = mongoClientConnection.validateCredentials("coolest_cat", "wrongPassword")
        if (!isInvalid) {
            println("Invalid credentials correctly identified.")
        } else {
            println("Error: Invalid credentials were not identified.")
        }

        // Test user retrieval
        val retrievedUser = mongoClientConnection.getUserProfileByUsername("coolest_cat")
        if (retrievedUser != null) {
            println("Retrieved user profile: $retrievedUser")
        } else {
            println("User profile not found.")
        }

        // Close the MongoDB connection
        mongoClientConnection.close()
    }
}