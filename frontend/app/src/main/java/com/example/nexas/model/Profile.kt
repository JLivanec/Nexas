package com.example.nexas.model

data class Profile(
    var id: String,
    var username: String,
    var firstName: String,
    var lastName: String,
    var location: String,
    var description: String,
    var avatar: String,
    var background: String,
    var age: Int
)