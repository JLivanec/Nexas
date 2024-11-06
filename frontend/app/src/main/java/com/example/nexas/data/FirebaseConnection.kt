package com.example.nexas.data

import android.net.Uri
import android.util.Log
import com.example.nexas.model.Group
import com.example.nexas.model.Message
import com.example.nexas.model.Profile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class FirebaseConnection() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var user: FirebaseUser? = auth.currentUser

    // Check if user is currently logged in
    fun loggedIn(): Boolean {
        return user != null
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
                location = document["location"].toString(),
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
            "location" to profile.location,
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

    // Fetches groups based on user id
    suspend fun getGroups(): List<Group> {
        return try {
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
                    location = document.getString("location") ?: "",
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

                val messagesSnapshot = db.collection("groups")
                    .document(document.id)
                    .collection("messages")
                    .get()
                    .await()

                group.messages = messagesSnapshot.documents.map { messageDoc ->
                    messageDoc.toObject(Message::class.java) // Assuming Message class is defined
                }.filterNotNull().toMutableList()

                groups.add(group)
            }

            Log.d("FirebaseConnection", "Fetched groups: ${groups.size}")
            groups
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error fetching groups", e)
            emptyList()
        }
    }

    suspend fun getProfile(userID: String): Profile? {
        return try {
            // Retrieve the profile document from Firestore
            val document = db.collection("profiles").document(userID).get().await()

            // Check if the document exists
            if (document.exists()) {
                // Construct and return the Profile object
                Profile(
                    id = userID,
                    username = document["username"].toString(),
                    firstName = document["firstName"].toString(),
                    lastName = document["lastName"].toString(),
                    location = document["location"].toString(),
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
    private suspend fun addBlockedUser(blockedId: String): Boolean {
        return try {
            val currentUserId = user?.uid ?: throw Exception("User is not logged in!")

            db.collection("profiles").document(currentUserId)
                .update("blockedUsers", FieldValue.arrayUnion(blockedId))
                .await()

            Log.d("FirebaseConnection", "User $blockedId successfully blocked by $currentUserId")
            true
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error blocking user", e)
            false
        }
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
    private suspend fun checkIfBlocked(otherUserId: String): Boolean {
        val blockedUsers = getBlockedUsers()
        return otherUserId in blockedUsers
    }
}