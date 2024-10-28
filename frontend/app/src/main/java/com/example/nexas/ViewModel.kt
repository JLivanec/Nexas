package com.example.nexas

import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ViewModel : ViewModel() {
    private val apiService: ApiService = Retrofit.Builder()
        .baseUrl("http://<your-server-ip>:3000/") // Update with server IP
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    private lateinit var myProfile: Member
    private var _groups = MutableLiveData<List<Group>>(emptyList())
    val groups: LiveData<List<Group>> get() = _groups

    fun fetchMyProfile() {
        viewModelScope.launch {
            try {
                myProfile = apiService.getMyProfile()
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun fetchGroups() {
        viewModelScope.launch {
            try {
                _groups.value = apiService.getGroups()
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun updateProfile(profile: Member) {
        viewModelScope.launch {
            try {
                myProfile = apiService.updateMyProfile(profile)
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun createGroup(group: Group) {
        viewModelScope.launch {
            try {
                val newGroup = apiService.createGroup(group)
                val updatedList = _groups.value?.toMutableList() ?: mutableListOf()
                updatedList.add(newGroup)
                _groups.value = updatedList
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun joinGroup(groupID: String) {

    }

    fun sendVideo(video: MediaStore.Video) {

    }

}