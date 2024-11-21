package com.example.nexas

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.nexas.data.FirebaseConnection
import com.example.nexas.model.Group
import com.example.nexas.model.Profile
import com.google.firebase.firestore.GeoPoint


class ViewModel(application: Application) : AndroidViewModel(application) {

    private var fb = FirebaseConnection()
    lateinit var myProfile: Profile
    private var groups = listOf<Group>()
    private var myGroups = listOf<Group>()

    fun getGroups(): List<Group> {
        return groups
    }

    fun getMyGroups(): List<Group> {
        return myGroups
    }

    suspend fun createAccount(email: String, password: String, profile: Profile): String {
        val error = fb.createUser(email, password, profile)

        if (error == "")
            myProfile = profile
        else {
            myProfile = Profile(
                id = "",
                firstName = "",
                lastName = "",
                username = "",
                location = GeoPoint(0.0, 0.0),
                description = "",
                avatar = "",
                background = "",
                age = -1,
            )
            fetchGroups()
        }
        return error
    }

    suspend fun login(email: String, password: String): String {
        val tempProfile = fb.login(email, password)
            ?: return "Error: Incorrect Username or Password"

        myProfile = tempProfile
        fetchGroups()
        return ""
    }

    suspend fun updateProfile(profile: Profile): String {
        return try {
            fb.updateUser(myProfile, profile)
            myProfile = fb.getProfile(myProfile.id)?: myProfile
            ""
        } catch (e: Exception) {
            e.message ?: "Failed to update profile"
        }
    }

    suspend fun createGroup(group: Group): String {
        return try {
            fb.createGroup(group) ?: return "Error: Group creation failed"

            fetchGroups()
            ""
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun fetchGroups() {
        try {
            groups = fb.getGroups()
            myGroups = fb.getMyGroups()
        } catch (e: Exception) {
            Log.e("FirebaseConnection", "Error fetching groups", e)
        }
    }

    suspend fun joinGroup(groupID: String) {
        fb.joinGroup(groupID)
        fetchGroups()
    }

    suspend fun leaveGroup(groupID: String) {
        fb.leaveGroup(groupID)
        fetchGroups()
    }

    suspend fun blockUser(profileID: String) {
        try {
            fb.blockUser(profileID)
        }
        catch (e: Exception) {
            Log.e("FirebaseConnection", "Error blocking user", e)
        }
    }

    suspend fun unblockUser(profileID: String) {
        try {
            fb.unblockUser(profileID)
        }
        catch (e: Exception) {
            Log.e("FirebaseConnection", "Error unblocking user", e)
        }
    }

    suspend fun isBlocked(profileID: String): Boolean {
        return fb.checkIfBlocked(profileID)
    }

    suspend fun sendVideo(videoURI: String, videoImageURI: String, groupId: String) {
        try {
            fb.sendVideo(videoURI, videoImageURI, groupId)
            fetchGroups()
        }
        catch (e: Exception) {
            Log.e("FirebaseConnection", "Error unblocking user", e)
        }
    }

    suspend fun setUserLocation(latLng: com.google.android.gms.maps.model.LatLng) {
        val geoPoint = GeoPoint(latLng.latitude, latLng.longitude)
        fb.setUserLocation(geoPoint)
        myProfile = fb.getProfile(myProfile.id)?: myProfile
    }

    fun findGroupById(targetId: String): Group? {
        return groups.find { it.id == targetId }
    }

    fun findMyGroupById(targetId: String): Group? {
        return myGroups.find { it.id == targetId }
    }

    fun findProfileById(targetId: String): Profile? {
        var user = myGroups.flatMap { it.members!! }.find { it.id == targetId }

        if (user == null)
            user = groups.flatMap { it.members!! }.find { it.id == targetId }

        return user
    }

    suspend fun autoLogin(): Boolean {
        val tempProfile = fb.loggedIn()
        if (tempProfile != null) {
            myProfile = tempProfile
            fetchGroups()
            return true
        }
        return false
    }

    fun logout() {
        fb.logout()
    }

}
