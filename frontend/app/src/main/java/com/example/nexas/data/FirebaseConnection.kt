package com.example.nexas.data

import android.net.Uri
import android.util.Log
import com.example.nexas.model.Group
import com.example.nexas.model.Message
import com.example.nexas.model.Profile
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirebaseConnection() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var user: FirebaseUser? = auth.currentUser

    // Check if user is currently logged in
    suspend fun loggedIn(): Profile? {
        if (user != null) {
            return try {
                val document = db.collection("profiles")
                    .document(user!!.uid)
                    .get()
                    .await()
                Log.d("FirebaseConnection", "Signed in - Success")

                // Construct profile
                Profile(
                    id = user!!.uid,
                    username = document["username"].toString(),
                    firstName = document["firstName"].toString(),
                    lastName = document["lastName"].toString(),
                    location = document.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                    description = document["description"].toString(),
                    avatar = document["avatar"].toString(),
                    background = document["background"].toString(),
                    age = document["age"].toString().toInt(),
                )
            } catch (e: Exception) {
                Log.w("FirebaseConnection", "Login failed", e)
                null
            }
        }

        return null
    }

    // Login user
    suspend fun login(email: String, password: String): Profile? {
        return try {
            // Sign in user
            val result = auth.signInWithEmailAndPassword(email, password).await()
            user = result.user ?: throw Exception("Sign-in failed")

            // Retrieve profile
            val document = db.collection("profiles")
                .document(user!!.uid)
                .get()
                .await()
            Log.d("FirebaseConnection", "Signed in - Success")

            // Construct profile
            Profile(
                id = user!!.uid,
                username = document["username"].toString(),
                firstName = document["firstName"].toString(),
                lastName = document["lastName"].toString(),
                location = document.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                description = document["description"].toString(),
                avatar = document["avatar"].toString(),
                background = document["background"].toString(),
                age = document["age"].toString().toInt(),
            )
        } catch (e: Exception) {
            Log.w("FirebaseConnection", "Login failed", e)
            null
        }
    }

    // Logout user
    fun logout() {
        auth.signOut()
        user = null
    }

    // Create a new user and a profile
    suspend fun createUser(email: String, password: String, profile: Profile): String {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            user = result.user ?: throw Exception("User creation failed")

            // Create profile
            val profileData = mapOf(
                "username" to profile.username,
                "firstName" to profile.firstName,
                "lastName" to profile.lastName,
                "location" to profile.location,
                "description" to profile.description,
                "avatar" to profile.avatar,
                "background" to profile.background,
                "age" to profile.age
            )
            db.collection("profiles").document(user!!.uid).set(profileData).await()
            Log.d("FirebaseConnection", "Wrote user profile")
            "" // No error
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error creating user", e)
            e.message ?: "Failed to create account"
        }
    }

    suspend fun updateUser(myProfile: Profile, profile: Profile) {
        val profileData = mutableMapOf<String, Any?>(
            "firstName" to profile.firstName,
            "lastName" to profile.lastName,
            "description" to profile.description,
            "age" to profile.age
        )

        if (myProfile.avatar != profile.avatar) {
            profileData["avatar"] = uploadImage(profile.avatar, "profileAvatars/${user!!.uid}.jpg")
        }

        if (myProfile.background != profile.background) {
            profileData["background"] = uploadImage(profile.background, "profileBackgrounds/${user!!.uid}.jpg")
        }

        db.collection("profiles").document(user!!.uid).update(profileData).await()
        Log.d("FirebaseConnection", "Wrote user profile")
    }

    suspend fun setUserLocation(geoPoint: GeoPoint) {
        val profileData = mutableMapOf<String, Any?>(
            "location" to geoPoint
        )

        db.collection("profiles").document(user!!.uid).update(profileData).await()
        Log.d("FirebaseConnection", "Updated user location")
    }

    // Create a new group with a random group ID
    suspend fun createGroup(group: Group): Group? {
        return try {
            val groupId = db.collection("groups").document().id

            val groupData = mapOf(
                "name" to group.name,
                "avatar" to uploadImage(group.avatar, "groupAvatars/${groupId}.jpg"),
                "location" to group.location,
                "description" to group.description,
                "membersLimit" to group.membersLimit,
                "members" to listOf(user!!.uid)
            )

            db.collection("groups")
                .document(groupId)
                .set(groupData)
                .await()

            Log.d("FirebaseConnection", "Wrote group with ID: $groupId")
            group.id = groupId
            group
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error creating group", e)
            throw e // Re-throw the exception
        }
    }

    suspend fun updateGroup(groupId: String, groupName: String, groupDescription: String) {
        try {

            val groupData = hashMapOf(
                "name" to groupName,
                "description" to groupDescription
            )

            db.collection("groups")
                .document(groupId)
                .update(groupData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("FirebaseConnection", "Group successfully updated!")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseConnection", "Error updating group", e)
                }

        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error updating group", e)
            throw e // Re-throw the exception
        }
    }

    // Fetches groups based on user id
    suspend fun getGroups(): List<Group> {
        val querySnapshot = db.collection("groups")
            .get()
            .await()

        val groups = mutableListOf<Group>()
        val currentUserId = user?.uid

        for (document in querySnapshot.documents) {
            val memberIds = document.get("members") as? List<String> ?: emptyList()

            // Check if the current user is a member of this group
            if (memberIds.contains(currentUserId)) {
                continue // Skip this group since the user is a member
            }

            val group = Group(
                id = document.id,
                name = document.getString("name") ?: "",
                avatar = document.getString("avatar") ?: "",
                location = document.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                description = document.getString("description") ?: "",
                membersLimit = document.getLong("membersLimit")?.toInt() ?: 0,
                members = mutableListOf(),
                messages = null
            )

            // Check if the current user is blocked by any member
            var isBlockedByAnyMember = false

            for (memberId in memberIds) {
                val blockedUsers = getBlockedUsersForMember(memberId.toString())
                if (blockedUsers.contains(currentUserId)) {
                    isBlockedByAnyMember = true
                    break // No need to check further if blocked by one member
                }
            }

            // Only add the group if the current user is not blocked by any member
            if (!isBlockedByAnyMember) {
                val memberProfiles = memberIds.mapNotNull { memberId ->
                    runCatching {
                        getProfile(memberId.toString())
                    }.getOrNull()
                }

                group.members = memberProfiles.toMutableList()
                groups.add(group)
            }
        }

        Log.d("FirebaseConnection", "Fetched groups: ${groups.size}")
        return groups
    }


    // Helper function to get the blocked users for a specific member
    private suspend fun getBlockedUsersForMember(memberId: String): List<String> {
        return try {
            val document = db.collection("profiles").document(memberId).get().await()
            document.get("blockedUsers") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error retrieving blocked users for member $memberId", e)
            emptyList()
        }
    }


    suspend fun getMyGroups(): List<Group> {
        val querySnapshot = db.collection("groups")
            .whereArrayContains("members", user!!.uid)
            .get()
            .await()

        val groups = mutableListOf<Group>()

        for (document in querySnapshot.documents) {
            val group = Group(
                id = document.id,
                name = document.getString("name") ?: "",
                avatar = document.getString("avatar") ?: "",
                location = document.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                description = document.getString("description") ?: "",
                membersLimit = document.getLong("membersLimit")?.toInt() ?: 0,
                members = mutableListOf(),
                messages = mutableListOf()
            )

            val memberIds = document.get("members") as? List<*> ?: emptyList<String>()

            val memberProfiles = memberIds.mapNotNull { memberId ->
                runCatching {
                    getProfile(memberId.toString())
                }.getOrNull()
            }

            group.members = memberProfiles.toMutableList()

            // Fetch messages from Firestore
            val messagesSnapshot = db.collection("groups")
                .document(document.id)
                .collection("messages")
                .get()
                .await()

            Log.d("CONNECTION SNAPSHOT", messagesSnapshot.documents.toString())

            // Manually create Message objects
            val messages = messagesSnapshot.documents.mapNotNull { messageDoc ->
                val id = messageDoc.id
                val senderID = messageDoc.getString("senderID") ?: return@mapNotNull null
                val videoImage = messageDoc.getString("videoImage") ?: return@mapNotNull null
                val video = messageDoc.getString("video") ?: return@mapNotNull null
                val timestamp = messageDoc.getTimestamp("timestamp") ?: return@mapNotNull null
                val likedBy = (messageDoc["likedBy"] as? List<String>)?.toMutableList() ?: mutableListOf()
                val pinned = messageDoc.getBoolean("pinned") ?: false
                Message(
                    id = id,
                    senderID = senderID,
                    videoImage = videoImage,
                    video = video,
                    timestamp = timestamp,
                    likedBy = likedBy,
                    pinned = pinned
                )

            }

            group.messages = messages.sortedBy { it.timestamp.toDate() }.toMutableList()

            groups.add(group)
        }

        Log.d("FirebaseConnection", "Fetched my groups: ${groups.size}")
        return groups
    }

    suspend fun likeMessage(groupId: String, messageId: String, userId: String) {
        try {
            db.collection("groups")
                .document(groupId)
                .collection("messages")
                .document(messageId)
                .update("likedBy", FieldValue.arrayUnion(userId))
                .await()
            Log.d("Chat", "User $userId liked message $messageId in group $groupId")
        } catch (e: Exception) {
            Log.e("Chat", "Error liking message $messageId", e)
        }
    }

    suspend fun unlikeMessage(groupId: String, messageId: String, userId: String) {
        try {
            db.collection("groups")
                .document(groupId)
                .collection("messages")
                .document(messageId)
                .update("likedBy", FieldValue.arrayRemove(userId))
                .await()
            Log.d("Chat", "User $userId unliked message $messageId in group $groupId")
        } catch (e: Exception) {
            Log.e("Chat", "Error unliking message $messageId", e)
        }
    }

    suspend fun pinVideo(groupId: String, messageId: String) {
        try {
            val messagesRef = db.collection("groups")
                .document(groupId)
                .collection("messages")

            // Fetch all messages in the group
            val messageSnapshot = messagesRef.get().await()

            // Use a batch to perform multiple updates atomically
            val batch = db.batch()

            // Iterate over each message
            for (messageDoc in messageSnapshot.documents) {
                val isClickedMessage = messageDoc.id == messageId
                val currentPinned = messageDoc.getBoolean("pinned") ?: false

                // Update only if the pinned status needs to be changed
                if (isClickedMessage && !currentPinned) {
                    // Set the clicked message's pinned attribute to true
                    Log.d("PINNED", "Pinning message true")
                    batch.update(messageDoc.reference, "pinned", true)
                } else if (!isClickedMessage && currentPinned) {
                    // Set all other messages' pinned attribute to false
                    Log.d("PINNED", "Pinning message false")
                    batch.update(messageDoc.reference, "pinned", false)
                }
            }

            // Commit the batch operation
            batch.commit().await()


            Log.d("PINNED", "Pinned message updated successfully for messageId: $messageId")

        } catch (e: Exception) {
            Log.e("Chat", "Error pinning message $messageId", e)
        }
    }

    suspend fun getLikes(groupId: String, messageId: String): List<String> {
        return try {
            val messageDoc = db.collection("groups")
                .document(groupId)
                .collection("messages")
                .document(messageId)
                .get()
                .await()

            val likedBy = messageDoc["likedBy"] as? List<String> ?: emptyList()
            Log.d("Chat", "Retrieved likes for message $messageId: $likedBy")
            likedBy
        } catch (e: Exception) {
            Log.e("Chat", "Error retrieving likes for message $messageId", e)
            emptyList()
        }
    }

    suspend fun getProfile(userID: String): Profile? {
        return try {
            val document = db.collection("profiles").document(userID).get().await()

            if (document.exists()) {
                Profile(
                    id = userID,
                    username = document["username"].toString(),
                    firstName = document["firstName"].toString(),
                    lastName = document["lastName"].toString(),
                    location = document.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                    description = document["description"].toString(),
                    avatar = document["avatar"].toString(),
                    background = document["background"].toString(),
                    age = document["age"].toString().toInt(),
                )
            } else {
                Log.w("FirebaseConnection", "No profile found for userID: $userID")
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error fetching profile for userID: $userID", e)
            null
        }
    }

    suspend fun joinGroup(groupId: String) {
        val userId = user!!.uid
        val groupDoc = db.collection("groups").document(groupId).get().await()

        if (groupDoc.exists()) {
            val members = groupDoc.get("members") as? List<String> ?: emptyList()

            if (!members.contains(userId)) {
                db.collection("groups")
                    .document(groupId)
                    .update("members", FieldValue.arrayUnion(userId))
                    .await()
            }
        }
    }


    suspend fun leaveGroup(groupId: String) {
        val userId = user!!.uid
        val groupDoc = db.collection("groups").document(groupId).get().await()

        if (groupDoc.exists()) {
            val members = groupDoc.get("members") as? List<String> ?: emptyList()

            if (members.contains(userId)) {
                // Remove the user from the group
                db.collection("groups")
                    .document(groupId)
                    .update("members", FieldValue.arrayRemove(userId))
                    .await()
            }
        }
    }

    private suspend fun uploadImage(imageUri: String, storageLoc: String): String {
        return try {
            val storageRef = storage.reference
            val imagesRef = storageRef.child(storageLoc)
            val uploadTask = imagesRef.putFile(Uri.parse(imageUri)).await()
            imagesRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error uploading image", e)
            throw e
        }
    }


    // add a blocked user to the list of blocked users contained in the BlockedProfiles object
    suspend fun blockUser(userId: String) {
        val currentUserId = user?.uid ?: throw Exception("User is not logged in!")

        db.collection("profiles").document(currentUserId)
            .update("blockedUsers", FieldValue.arrayUnion(userId))
            .await()

        Log.d("FirebaseConnection", "User $userId successfully blocked")
    }

    suspend fun unblockUser(userId: String) {
        val currentUserId = user?.uid ?: throw Exception("User is not logged in!")
        val profileDoc = db.collection("profiles").document(currentUserId).get().await()

        if (profileDoc.exists()) {
            val blockedUsers = profileDoc.get("blockedUsers") as? List<String> ?: emptyList()

            if (blockedUsers.contains(userId)) {
                db.collection("profiles")
                    .document(currentUserId)
                    .update("blockedUsers", FieldValue.arrayRemove(userId))
                    .await()
            }
        }

        Log.d("FirebaseConnection", "User $userId successfully unblocked")
    }

    // retrieve a list of blocked users for the given profile
    private suspend fun getBlockedUsers(): List<String> {
        return try {
            val currentUserId = user?.uid ?: throw Exception("User is not logged in!")
            val document = db.collection("profiles").document(currentUserId).get().await()

            document["blockedUsers"] as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error retrieving blocked users", e)
            emptyList()
        }
    }

    // check if another profile is blocked
    suspend fun checkIfBlocked(otherUserId: String): Boolean {
        val blockedUsers = getBlockedUsers()
        return otherUserId in blockedUsers
    }

    suspend fun sendVideo(videoURI: String, videoImageURI: String, groupId: String) {
        try {
            // First, upload the video to Firebase Storage
            val messageId = db.collection("groups").document().id
            val videoUrl = uploadVideo(videoURI, "groupMessages/$groupId/${messageId}.mp4")
            val videoImageUrl = uploadImage(videoImageURI, "groupMessages/$groupId/${messageId}.jpg")

            // Create a message object
            val message = Message(
                id = messageId,
                senderID = user!!.uid,
                videoImage = videoImageUrl,
                video = videoUrl,
                pinned = false
            )

            // Add the message to the Firestore group messages collection
            db.collection("groups")
                .document(groupId)
                .collection("messages")
                .add(message)
                .await()

            Log.d("FirebaseConnection", "Video sent successfully: $videoUrl")
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error sending video", e)
        }
    }

    private suspend fun uploadVideo(videoUri: String, storageLoc: String): String {
        return try {
            val storageRef = storage.reference
            val videoRef = storageRef.child(storageLoc)
            val uploadTask = videoRef.putFile(Uri.parse(videoUri)).await()
            videoRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error uploading video", e)
            throw e
        }
    }

}