package com.example.nexas

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("myProfile")
    suspend fun getMyProfile(): Member

    @POST("myProfile")
    suspend fun updateMyProfile(@Body profile: Member): Member

    @GET("groups")
    suspend fun getGroups(): List<Group>

    @POST("groups")
    suspend fun createGroup(@Body group: Group): Group
}