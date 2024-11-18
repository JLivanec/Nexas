package com.example.nexas

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.nexas.data.CustomLocation
import com.example.nexas.data.FirebaseConnection
import com.example.nexas.data.stateAbbreviations
import com.example.nexas.model.Group
import com.example.nexas.model.Profile
import kotlinx.serialization.json.Json
import okio.IOException
import java.util.Locale


class ViewModel(application: Application) : AndroidViewModel(application) {

    private var fb = FirebaseConnection()
    lateinit var myProfile: Profile
    private var _groups = MutableLiveData<List<Group>>(emptyList())
    val groups: MutableLiveData<List<Group>> get() = _groups

    private var _address: Address? = null
    val address get() = _address
    val locality: String get() = if (address != null) address!!.locality else "Error getting locality."
    val state: String get() = if (address != null) stateAbbreviations.getOrDefault(address!!.adminArea, address!!.adminArea) else "Error getting state."


    // Sets the address of the user on login
    fun setAddress(context: Context) {
        try {
            val locationData = Json.decodeFromString<CustomLocation>(myProfile.location)
            val geocoder = Geocoder(context, Locale.getDefault())
            _address = geocoder.getFromLocation(locationData.latitude, locationData.longitude, 1)!![0]
        }  catch (e : IOException) {
            e.printStackTrace();
            _address = null
        }
    }

    suspend fun createAccount(email: String, password: String, profile: Profile): String {
        val error = fb.createUser(email, password, profile)

        if (error == "")
            myProfile = profile
        else
            myProfile = Profile(
                id = "",
                firstName = "",
                lastName = "",
                username = "",
                location = "",
                description = "",
                avatar = "",
                background = "",
                age = -1,
            )

        return error
    }

    suspend fun login(email: String, password: String): String {
        val tempProfile = fb.login(email, password)
            ?: return "Error: Incorrect Username or Password"

        myProfile = tempProfile
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
            val createdGroup = fb.createGroup(group)
                ?: return "Error: Group creation failed"

            val currentGroups = _groups.value?.toMutableList() ?: mutableListOf()
            currentGroups.add(createdGroup)
            _groups.value = currentGroups
            ""
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun getGroups() {
        try {
            val fetchedGroups = fb.getGroups()
            _groups.value = fetchedGroups
        } catch (e: Exception) {
            // Handle errors if needed
        }
    }

    fun joinGroup(groupID: String) {
        // TODO: Join group
    }

    fun sendVideo(video: MediaStore.Video) {
        // TODO: Send video
    }

    fun findGroupById(targetId: String): Group? {
        return groups.value?.find { it.id == targetId }
    }

//    suspend fun autoLogin(): Boolean {
//        if (myProfile.hashedPassword != "") {
//            val tempProfile = db.validateCredentials(myProfile.username, myProfile.hashedPassword)
//                ?: return false
//
//            myProfile = tempProfile
//            return true
//        }
//        return false
//    }

//    private suspend fun validateProfile(profile: Profile): String {
//        if (profile.firstName == "" ||
//            profile.firstName.contains(" "))
//            return "Error: Invalid First Name (No spaces allowed)"
//
//        if (profile.lastName == "" ||
//            profile.lastName.contains(" "))
//            return "Error: Invalid Last Name (No spaces allowed)"
//
//        if (profile.username == "" ||
//            profile.username.contains(" "))
//            return "Error: Invalid Username (No spaces allowed)"
//
//        if (profile.email == "" || profile.email.contains(" "))
//            return "Error: Invalid Email"
//
//        if (db.getProfileByUsername(profile.username) != null)
//            return "Error: An account with this username already exists"
//
//        return ""
//    }

}
