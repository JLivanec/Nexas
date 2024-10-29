package com.example.nexas

import com.example.nexas.data.MongoClientConnection
import com.example.nexas.model.UserProfile
import kotlinx.coroutines.runBlocking
import org.mindrot.jbcrypt.BCrypt

fun main() {
    runBlocking {
        val mongoClientConnection = MongoClientConnection()

        // Test data for user profile
        val testUser = UserProfile(
            userID = "123456789",
            uname = "coolest_cat",
            fname = "Cool",
            lname = "Cat",
            email = "lowtemperaturefeline@example.com",
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

        // Close the MongoDB connection
        mongoClientConnection.close()

        // Test user retrieval
        val retrievedUser = mongoClientConnection.getUserProfileByUsername("test_user")
        if (retrievedUser != null) {
            println("Retrieved user profile: $retrievedUser")
        } else {
            println("User profile not found.")
        }
    }
}
