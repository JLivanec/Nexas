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
import org.bson.types.ObjectId


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

    suspend fun getUserProfileByID(id: String): UserProfile? {
        // Find user by ID as a Document
        val userDocument = userCollection.find(eq("_id", ObjectId(id))).firstOrNull()

        // Convert Document to UserProfile if found
        return userDocument?.let {
            UserProfile(
                id = it.getString("id"),
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
                hashedPassword = it.getString("hashedPassword") ?: ""
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
            messageCollection.find(eq("groupID", ObjectId(groupID))).toList().map { document ->
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

    suspend fun getMessageByID(id: String): Message? {
        // Find message by ID as a Document
        val messageDocument = messageCollection.find(eq("_id", ObjectId(id))).firstOrNull()

        // Convert Document to Message if found
        return messageDocument?.let {
            Message(
                id = it.getString("id"),
                senderID = it.getString("senderID"),
                groupID = it.getString("groupID"),
                videoImage = it.get("videoImage", ByteArray::class.java)?.let { byteArray ->
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                },
                videoID = it.getString("videoID")
            )
        }
    }


    // Function to remove a message by ID
    suspend fun deleteMessageByID(id: String): Boolean {
        return try {
            val result = messageCollection.deleteOne(eq("_id", ObjectId(id)))
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

    // Function to insert a new group into the database
    suspend fun insertGroup(group: Group): Boolean {
        return try {
            // Create a Document for the group, handling the avatar as ByteArray
            val document = Document()
                .append("name", group.name)
                .append("avatar", group.avatar?.toByteArray()) // Convert Bitmap to ByteArray
                .append("location", group.location)
                .append("description", group.description)
                .append("membersLimit", group.membersLimit)
                .append("members", group.members?.map { it.id } ?: emptyList<UserProfile>()) // Store only the IDs
                .append("messages", group.messages?.map { it.id } ?: emptyList<Message>()) // Store only the IDs

            groupCollection.insertOne(document)
            true // Insertion was successful
        } catch (e: Exception) {
            e.printStackTrace()
            false // Insertion failed
        }
    }

    // Function to get all groups by name
    suspend fun getGroupByName(name: String): List<Group> {
        return try {
            val groupDocuments = groupCollection.find(eq("name", name)).toList() // Find all groups matching the name

            // Convert the Document list to Group objects
            groupDocuments.map { document ->
                Group(
                    id = document.getObjectId("_id").toHexString(), // Convert ObjectId to String
                    name = document.getString("name"),
                    avatar = document.get("avatar", ByteArray::class.java)?.let { byteArray ->
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    },
                    location = document.getString("location"),
                    description = document.getString("description"),
                    membersLimit = document.getInteger("membersLimit"),
                    members = document.getList("members", String::class.java).map { memberId ->
                        // Fetch UserProfile for each member ID (implement this method as needed)
                        getUserProfileByID(memberId) ?: UserProfile(
                            id = memberId, // Fallback ID
                            uname = "Unknown", // Fallback uname
                            fname = "Unknown", // Fallback fname
                            lname = "Unknown", // Fallback lname
                            email = "unknown@example.com", // Fallback email
                            location = "Unknown", // Fallback location
                            description = "User profile not found.", // Fallback description
                            avatar = null, // No avatar for unknown user
                            background = null, // No background for unknown user
                            age = 0, // Fallback age
                            hashedPassword = "" // No hashed password for unknown user
                        ) // Fallback if user not found
                    },
                    messages = document.getList("messages", String::class.java).map { messageId ->
                        // Fetch Message for each message ID (implement this method as needed)
                        getMessageByID(messageId) ?: Message(
                            id = messageId, // Fallback ID
                            senderID = "Unknown", // Fallback senderID
                            groupID = "Unknown", // Fallback groupID
                            videoID = "Unknown", // Fallback videoID
                            timestamp = Instant.now(), // Fallback timestamp
                            videoImage = null // No videoImage for unknown message
                        ) // Fallback if message not found
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Return an empty list in case of error
        }
    }

    // Function to get a group by ID
    suspend fun getGroupById(id: String): Group? {
        return try {
            val groupDocument = groupCollection.find(eq("_id", ObjectId(id))).firstOrNull() // Find group by ID

            // Convert Document to Group if found
            groupDocument?.let {
                Group(
                    id = it.getObjectId("_id").toHexString(), // Convert ObjectId to String
                    name = it.getString("name"),
                    avatar = it.get("avatar", ByteArray::class.java)?.let { byteArray ->
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    },
                    location = it.getString("location"),
                    description = it.getString("description"),
                    membersLimit = it.getInteger("membersLimit"),
                    members = it.getList("members", String::class.java).map { memberId ->
                        // Fetch UserProfile for each member ID
                        getUserProfileByID(memberId) ?: UserProfile(
                            id = memberId, // Fallback ID
                            uname = "Unknown", // Fallback uname
                            fname = "Unknown", // Fallback fname
                            lname = "Unknown", // Fallback lname
                            email = "unknown@example.com", // Fallback email
                            location = "Unknown", // Fallback location
                            description = "User profile not found.", // Fallback description
                            avatar = null, // No avatar for unknown user
                            background = null, // No background for unknown user
                            age = 0, // Fallback age
                            hashedPassword = "" // No hashed password for unknown user
                        ) // Fallback if user not found
                    },
                    messages = it.getList("messages", String::class.java).map { messageId ->
                        // Fetch Message for each message ID
                        getMessageByID(messageId) ?: Message(
                            id = messageId, // Fallback ID
                            senderID = "Unknown", // Fallback senderID
                            groupID = "Unknown", // Fallback groupID
                            videoID = "Unknown", // Fallback videoID
                            timestamp = Instant.now(), // Fallback timestamp
                            videoImage = null // No videoImage for unknown message
                        ) // Fallback if message not found
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if not found
        }
    }

    suspend fun addMemberToGroup(groupId: String, userProfile: UserProfile): Boolean {
        return try {
            // Update the group's members list by adding the new member's ID
            val result = groupCollection.updateOne(
                eq("_id", ObjectId(groupId)),
                Document("\$addToSet", Document("members", userProfile.id))
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Function to send a message to a group
    suspend fun sendMessage(groupId: String, message: Message): Boolean {
        return try {
            // Insert the message into the message collection
            val insertSuccess = insertMessage(message)

            // If the message was inserted successfully, update the group's messages list
            if (insertSuccess) {
                val result = groupCollection.updateOne(
                    eq("_id", ObjectId(groupId)),
                    Document("\$addToSet", Document("messages", message.id)) // Add message ID to messages
                )
                result.modifiedCount > 0
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}