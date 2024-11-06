package com.example.nexas.model

data class BlockedProfiles(
    val blockerId: String,
    val blockedUsers: MutableList<String> = mutableListOf()
)
