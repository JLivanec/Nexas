package com.example.nexas.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.nexas.model.*
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
import java.time.Instant
import kotlinx.coroutines.flow.toList

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
            .codecRegistry(codecRegistry)
            .build()

        MongoClient.create(mongoClientSettings)
    }

    // Define the collections
    // using lazy so the entire database isn't queried every time this class is instantiated
    private val userCollection by lazy { mongoClient.getDatabase("Nexas").getCollection<Document>("userProfiles") }
    private val messageCollection by lazy { mongoClient.getDatabase("Nexas").getCollection<Document>("messages") }
    private val groupCollection by lazy { mongoClient.getDatabase("Nexas").getCollection<Document>("groups") }

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

    // Function to write a message to the database
    suspend fun insertMessage(message: Message): Boolean {
        return try {
            val document = Document()
                .append("id", message.id)
                .append("senderID", message.senderID)
                .append("groupID", message.groupID)
                .append("videoID", message.videoID)
                .append("timestamp", message.timestamp.toEpochMilli())
                .apply {
                    // Convert the videoImage Bitmap to ByteArray if it exists
                    message.videoImage?.let { bitmap ->
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        put("videoImage", stream.toByteArray())
                    }
                }

            messageCollection.insertOne(document)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Function to retrieve all messages by groupID
    suspend fun getMessagesByGroupID(groupID: String): List<Message> {
        return try {
            messageCollection.find(eq("groupID", groupID)).toList().map { document ->
                Message(
                    id = document.getString("id"),
                    senderID = document.getString("senderID"),
                    groupID = document.getString("groupID"),
                    videoID = document.getString("videoID"),
                    timestamp = Instant.ofEpochMilli(document.getLong("timestamp")),
                    videoImage = document.get("videoImage", ByteArray::class.java)?.let { byteArray ->
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Function to remove a message by ID
    suspend fun deleteMessageByID(messageID: String): Boolean {
        return try {
            val result = messageCollection.deleteOne(eq("id", messageID))
            result.deletedCount > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
