package com.example.nexas.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.ByteArrayOutputStream

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
    private val userCollection: MongoCollection<Document> = mongoClient.getDatabase("Nexas").getCollection("userProfiles")

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

    fun ByteArray.toBitmap(): Bitmap? {
        return BitmapFactory.decodeByteArray(this, 0, this.size)
    }

    suspend fun insertUserProfile(userProfile: UserProfile): Boolean {
        return try {
            // Manually create a Document to handle image conversion
            val document = Document()
                .append("uname", userProfile.uname)
                .append("fname", userProfile.fname)
                .append("lname", userProfile.lname)
                .append("email", userProfile.email)
                .append("location", userProfile.location)
                .append("description", userProfile.description)
                .append("age", userProfile.age)
                .append("hashedPassword", userProfile.hashedPassword)
                .append("avatar", userProfile.avatar?.toByteArray()) // Convert Bitmap to ByteArray
                .append("background", userProfile.background?.toByteArray()) // Convert Bitmap to ByteArray

            userCollection.insertOne(document) // Insert the document
            true // Insertion was successful
        } catch (e: Exception) {
            e.printStackTrace()
            false // Insertion failed
        }
    }

    suspend fun validateCredentials(username: String, password: String): Boolean {
        // Find user by username
        val userDocument = userCollection.find(eq("uname", username)).firstOrNull()

        val user = userDocument?.let {
            UserProfile(
                uname = it.getString("uname"),
                fname = it.getString("fname"),
                lname = it.getString("lname"),
                email = it.getString("email"),
                location = it.getString("location"),
                description = it.getString("description"),
                avatar = it.get("avatar", ByteArray::class.java)?.toBitmap(),
                background = it.get("background", ByteArray::class.java)?.toBitmap(),
                age = it.getInteger("age") ?: 0,
                hashedPassword = it.getString("hashedPassword") ?: "",
                id = it.getObjectId("_id")?.toHexString()
            )
        }
        // Check if user is not null and password matches
        return user != null && BCrypt.checkpw(password, user.hashedPassword)
    }

    suspend fun getUserProfileByUsername(username: String): UserProfile? {
        // Find user by username as a Document
        val userDocument = userCollection.find(eq("uname", username)).firstOrNull()

        // Convert Document to UserProfile if found
        return userDocument?.let {
            UserProfile(
                uname = it.getString("uname"),
                fname = it.getString("fname"),
                lname = it.getString("lname"),
                email = it.getString("email"),
                location = it.getString("location"),
                description = it.getString("description"),
                avatar = it.get("avatar", ByteArray::class.java)?.let { byteArray ->
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                },
                background = it.get("background", ByteArray::class.java)?.let { byteArray ->
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                },
                age = it.getInteger("age") ?: 0,
                hashedPassword = it.getString("hashedPassword") ?: "",
                id = it.getObjectId("_id")?.toHexString()
            )
        }
    }

    fun close() {
        mongoClient.close()
    }

    // Extension function to convert Bitmap to ByteArray
    private fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}

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
