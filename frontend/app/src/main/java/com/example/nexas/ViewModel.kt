package com.example.nexas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModel : ViewModel() {
    lateinit var myProfile: Member
    private var _groups = MutableLiveData<List<Group>>(emptyList())
    val groups: LiveData<List<Group>> get() = _groups

    init {
        fetchMyProfile()
        fetchGroups()
    }

    // TODO: Remove
    fun createSampleBitmap(color: Int): Bitmap {
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawColor(color)
        }
    }

    fun fetchMyProfile() {
        // TODO: Get My Profile
        myProfile = Member(
            id = "1209",
            name = "Jordan Bells",
            age = 22,
            avatar = null,
            location = "San Francisco, CA",
            description = "Hi, I’m Jordan! I’m a 22-year-old who just graduated from college and moved to San Francisco for my new job. I’m passionate about the outdoors and enjoy activities like hiking and climbing. While I prefer being around familiar faces, I’m excited to explore new places and meet new friends along the way.",
            background = null
        )
    }

    fun fetchGroups() {
        // TODO: Get groups from API
        val fakeGroups: List<Group> = listOf(
            Group(
                id = "146",
                name = "Bread Baking",
                avatar = createSampleBitmap(Color.MAGENTA),
                location = "San Francisco, CA",
                description = "Join us to share recipes, tips, and experiences in the art of bread baking. All skill levels welcome!",
                membersLimit = 10,
                members = listOf(
                    Member(
                        id = "1209",
                        name = "Jordan Bells",
                        age = 22,
                        avatar = createSampleBitmap(Color.RED),
                        location = "San Francisco, CA",
                        description = "Hi, I’m Jordan! I’m a 22-year-old who just graduated from college and moved to San Francisco for my new job. I love trying new things, and baking has become a fun new hobby for me!",
                        background = null
                    ),
                    Member(
                        id = "8921",
                        name = "David Lee",
                        age = 35,
                        avatar = createSampleBitmap(Color.BLUE),
                        location = "New York, NY",
                        description = "I’m David, a 35-year-old IT specialist who enjoys the science of baking. I'm excited to learn and share tips on making the perfect loaf.",
                        background = null
                    ),
                    Member(
                        id = "3472",
                        name = "Mildred Wilson",
                        age = 70,
                        avatar = createSampleBitmap(Color.BLACK),
                        location = "Chicago, IL",
                        description = "I'm Mildred, a 70-year-old retiree. While I might not ride myself, I love hearing stories about biking adventures and exploring nature.",
                        background = null
                    ),
                    Member(
                        id = "5783",
                        name = "Maryam Inaya Khan",
                        age = 25,
                        avatar = createSampleBitmap(Color.YELLOW),
                        location = "Los Angeles, CA",
                        description = "Hey, I’m Maryam, a 25-year-old history lover from LA. I find Ancient Rome fascinating and look forward to deepening my knowledge with you all!",
                        background = null
                    )
                ),
                messages = listOf(
                    Message(
                        id = "1",
                        senderID = "1209",
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "1"
                    ),
                    Message(
                        id = "2",
                        senderID = "8921",
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "2"
                    ),
                    Message(
                        id = "3",
                        senderID = "1209",
                        videoImage = null,
                        videoID = "3"
                    ),
                    Message(
                        id = "4",
                        senderID = "3472",
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "4"
                    ),
                    Message(
                        id = "5",
                        senderID = "1209",
                        videoImage = null,
                        videoID = "5"
                    )
                )
            ),
            Group(
                id = "679",
                name = "Mountain Biking",
                avatar = createSampleBitmap(Color.WHITE),
                location = "New York, NY",
                description = "A community for mountain biking enthusiasts to connect, share trails, and plan rides together.",
                membersLimit = 5,
                members = listOf(
                    Member(
                        id = "1209",
                        name = "Jordan Bells",
                        age = 22,
                        avatar = createSampleBitmap(Color.RED),
                        location = "San Francisco, CA",
                        description = "Hi, I’m Jordan! I’m a 22-year-old outdoor enthusiast who loves mountain biking and exploring new trails. I'm excited to connect with fellow bikers!",
                        background = null
                    ),
                    Member(
                        id = "8921",
                        name = "David Lee",
                        age = 35,
                        avatar = createSampleBitmap(Color.BLUE),
                        location = "New York, NY",
                        description = "I’m David, a 35-year-old IT specialist who enjoys the science of baking. I'm excited to learn and share tips on making the perfect loaf.",
                        background = null
                    ),
                    Member(
                        id = "6540",
                        name = "Jade Chan",
                        age = 20,
                        avatar = createSampleBitmap(Color.GREEN),
                        location = "Seattle, WA",
                        description = "Hello! I'm Jade, a 20-year-old online student. I love exploring history, and Ancient Rome is one of my favorite topics!",
                        background = null
                    )
                ),
                messages = listOf(
                    Message(
                        id = "1",
                        senderID = "1209",
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "1"
                    ),
                    Message(
                        id = "2",
                        senderID = "8921",
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "2"
                    ),
                    Message(
                        id = "3",
                        senderID = "1209",
                        videoImage = null,
                        videoID = "3"
                    ),
                    Message(
                        id = "4",
                        senderID = "6540",
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "4"
                    ),
                    Message(
                        id = "5",
                        senderID = "1209",
                        videoImage = null,
                        videoID = "5"
                    )
                )
            ),
            Group(
                id = "925",
                name = "Ancient Roman History",
                avatar = createSampleBitmap(Color.YELLOW),
                location = "Seattle, WA",
                description = "Dive into the fascinating world of Ancient Rome! Discussions, readings, and sharing resources welcome.",
                membersLimit = 20,
                members = listOf(
                    Member(
                        id = "1209",
                        name = "Jordan Bells",
                        age = 22,
                        avatar = createSampleBitmap(Color.RED),
                        location = "San Francisco, CA",
                        description = "Hi, I’m Jordan! I’m a recent grad with a keen interest in history. Excited to learn more about Ancient Rome and share insights!",
                        background = null
                    ),
                    Member(
                        id = "5783",
                        name = "Maryam Inaya Khan",
                        age = 25,
                        avatar = createSampleBitmap(Color.YELLOW),
                        location = "Los Angeles, CA",
                        description = "Hey, I’m Maryam, a 25-year-old history lover from LA. I find Ancient Rome fascinating and look forward to deepening my knowledge with you all!",
                        background = null
                    ),
                    Member(
                        id = "6540",
                        name = "Jade Chan",
                        age = 20,
                        avatar = createSampleBitmap(Color.GREEN),
                        location = "Seattle, WA",
                        description = "Hello! I'm Jade, a 20-year-old online student. I love exploring history, and Ancient Rome is one of my favorite topics!",
                        background = null
                    )
                ),
                messages = listOf(
                    Message(
                        id = "1",
                        senderID = "1209",
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "1"
                    ),
                    Message(
                        id = "2",
                        senderID = "5783",
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "2"
                    ),
                    Message(
                        id = "3",
                        senderID = "1209",
                        videoImage = null,
                        videoID = "3"
                    ),
                    Message(
                        id = "4",
                        senderID = "6540",
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "4"
                    ),
                    Message(
                        id = "5",
                        senderID = "1209",
                        videoImage = null,
                        videoID = "5"
                    )
                )
            )
        )
        _groups.value = fakeGroups
    }

    fun updateProfile(profile: Member) {
        // TODO: Save My Profile
        myProfile = profile
    }

    fun createGroup(group: Group): String {
        // TODO: Replace with making group in DB
        val newGroup = group.copy(id = "new${_groups.value!!.size + 1}",
            location = myProfile.location,
            members = listOf(myProfile)
        )
        val updatedList = _groups.value?.toMutableList() ?: mutableListOf()
        updatedList.add(newGroup)
        _groups.value = updatedList
        return ""
    }

    fun joinGroup(groupID: String) {
        // TODO: Join group
    }

    fun sendVideo(video: MediaStore.Video) {
        // TODO: Send video
    }
}
