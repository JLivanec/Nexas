package com.example.nexas.data

import com.example.nexas.model.UserProfile
import com.example.nexas.model.UserProfileCodec
import org.bson.Document
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.runBlocking
import org.mindrot.jbcrypt.BCrypt
import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

class MongoClientConnection {

    private val connectionString = "mongodb+srv://jxn:Bp3XAGF7LUkphcuc@nexas.52jkh.mongodb.net/?retryWrites=true&w=majority&appName=Nexas"

    private val mongoClient = run {
        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()

        val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
            CodecRegistries.fromCodecs(UserProfileCodec()),
            MongoClientSettings.getDefaultCodecRegistry()
        )

        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .serverApi(serverApi)
            .codecRegistry(codecRegistry) // Register the codec registry here
            .build()

        MongoClient.create(mongoClientSettings)
    }

    // Define the collection as a class property
    private val userCollection: MongoCollection<UserProfile> = mongoClient.getDatabase("Nexas").getCollection("userProfiles")

    init {
        runBlocking {
            try {
                val database = mongoClient.getDatabase("admin")
                database.runCommand(Document("ping", 1))
                println("Pinged your deployment. You successfully connected to MongoDB!")
            } catch (e: Exception) {
                e.printStackTrace()
                println("Failed to connect to MongoDB: ${e.message}")
            }
        }
    }

    suspend fun insertUserProfile(userProfile: UserProfile): Boolean {
        return try {
            userCollection.insertOne(userProfile) // Insert the profile
            true // Insertion was successful
        } catch (e: Exception) {
            e.printStackTrace()
            false // Insertion failed
        }
    }

    suspend fun validateCredentials(username: String, password: String): Boolean {
        // Find user by username
        val user = userCollection.find(eq("uname", username)).firstOrNull()
        // Check if user is not null and password matches
        return user != null && BCrypt.checkpw(password, user.hashedPassword)
    }

    suspend fun getUserProfileByUsername(username: String): UserProfile? {
        // Find user by username
        return userCollection.find(eq("uname", username)).firstOrNull()
    }

    fun close() {
        mongoClient.close()
    }
}
