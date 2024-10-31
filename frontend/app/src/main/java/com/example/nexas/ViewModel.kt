package com.example.nexas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexas.model.*

class ViewModel : ViewModel() {
    lateinit var myProfile: UserProfile
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
        myProfile = UserProfile(
            id = "1209",
            uname = "jordan_bells",
            fname = "Jordan",
            lname = "Bells",
            email = "jordan.bells@example.com",
            location = "San Francisco, CA",
            description = "Hi, I’m Jordan! I’m a 22-year-old who just graduated from college and moved to San Francisco for my new job. I’m passionate about the outdoors and enjoy activities like hiking and climbing. While I prefer being around familiar faces, I’m excited to explore new places and meet new friends along the way.",
            avatar = null,
            background = null,
            age = 22,
            hashedPassword = "hashedPasswordForJordan"
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
                    UserProfile(
                        id = "1209",
                        uname = "jordan_bells",
                        fname = "Jordan",
                        lname = "Bells",
                        email = "jordan.bells@example.com",
                        location = "San Francisco, CA",
                        description = "Hi, I’m Jordan! I’m a 22-year-old outdoor enthusiast who loves mountain biking and exploring new trails. I'm excited to connect with fellow bikers!",
                        avatar = createSampleBitmap(Color.RED),
                        background = null,
                        age = 22,
                        hashedPassword = "hashedPasswordForJordan"
                    ),
                    UserProfile(
                        id = "8921",
                        uname = "david_lee",
                        fname = "David",
                        lname = "Lee",
                        email = "david.lee@example.com",
                        location = "New York, NY",
                        description = "I’m David, a 35-year-old IT specialist who enjoys the science of baking. I'm excited to learn and share tips on making the perfect loaf.",
                        avatar = createSampleBitmap(Color.BLUE),
                        background = null,
                        age = 35,
                        hashedPassword = "hashedPasswordForDavid"
                    ),
                    UserProfile(
                        id = "6540",
                        uname = "jade_chan",
                        fname = "Jade",
                        lname = "Chan",
                        email = "jade.chan@example.com",
                        location = "Seattle, WA",
                        description = "Hello! I'm Jade, a 20-year-old online student. I love exploring history, and Ancient Rome is one of my favorite topics!",
                        avatar = createSampleBitmap(Color.GREEN),
                        background = null,
                        age = 20,
                        hashedPassword = "hashedPasswordForJade"
                    )
                ),
                messages = listOf(
                    Message(
                        id = "1",
                        senderID = "1209", // Corresponds to Jordan Bells
                        groupID = "146", // Corresponds to this group
                        videoImage = createSampleBitmap(Color.RED),
                        videoID = "First Ride Adventure"
                    ),
                    Message(
                        id = "2",
                        senderID = "8921", // Corresponds to David Lee
                        groupID = "146", // Corresponds to this group
                        videoImage = createSampleBitmap(Color.BLUE),
                        videoID = "Baking Science Explained"
                    ),
                    Message(
                        id = "3",
                        senderID = "1209", // Corresponds to Jordan Bells
                        groupID = "146", // Corresponds to this group
                        videoImage = null,
                        videoID = "Trail Safety Tips"
                    ),
                    Message(
                        id = "4",
                        senderID = "6540", // Corresponds to Jade Chan
                        groupID = "146", // Corresponds to this group
                        videoImage = createSampleBitmap(Color.GREEN),
                        videoID = "Exploring Ancient Rome"
                    ),
                    Message(
                        id = "5",
                        senderID = "1209", // Corresponds to Jordan Bells
                        groupID = "146", // Corresponds to this group
                        videoImage = null,
                        videoID = "Mountain Biking 101"
                    )
                )
            )
        )
        Group(
            id = "147",
            name = "Mountain Biking Adventures",
            avatar = createSampleBitmap(Color.YELLOW),
            location = "Boulder, CO",
            description = "Join us for thrilling mountain biking adventures! Share trails, tips, and ride experiences.",
            membersLimit = 15,
            members = listOf(
                UserProfile(
                    id = "1209",
                    uname = "jordan_bells",
                    fname = "Jordan",
                    lname = "Bells",
                    email = "jordan.bells@example.com",
                    location = "San Francisco, CA",
                    description = "Hi, I’m Jordan! I’m a 22-year-old outdoor enthusiast who loves mountain biking and exploring new trails. I'm excited to connect with fellow bikers!",
                    avatar = createSampleBitmap(Color.RED),
                    background = null,
                    age = 22,
                    hashedPassword = "hashedPasswordForJordan" // Placeholder for demonstration
                ),
                UserProfile(
                    id = "8921",
                    uname = "david_lee",
                    fname = "David",
                    lname = "Lee",
                    email = "david.lee@example.com",
                    location = "New York, NY",
                    description = "I’m David, a 35-year-old IT specialist who enjoys the science of baking. I'm excited to learn and share tips on making the perfect loaf.",
                    avatar = createSampleBitmap(Color.BLUE),
                    background = null,
                    age = 35,
                    hashedPassword = "hashedPasswordForDavid" // Placeholder for demonstration
                ),
                UserProfile(
                    id = "6540",
                    uname = "jade_chan",
                    fname = "Jade",
                    lname = "Chan",
                    email = "jade.chan@example.com",
                    location = "Seattle, WA",
                    description = "Hello! I'm Jade, a 20-year-old online student. I love exploring history, and Ancient Rome is one of my favorite topics!",
                    avatar = createSampleBitmap(Color.GREEN),
                    background = null,
                    age = 20,
                    hashedPassword = "hashedPasswordForJade" // Placeholder for demonstration
                ),
                UserProfile(
                    id = "3472",
                    uname = "max_walker",
                    fname = "Max",
                    lname = "Walker",
                    email = "max.walker@example.com",
                    location = "Boulder, CO",
                    description = "I'm Max, a passionate mountain biker and trail guide. Let's ride some epic trails together!",
                    avatar = createSampleBitmap(Color.CYAN),
                    background = null,
                    age = 30,
                    hashedPassword = "hashedPasswordForMax" // Placeholder for demonstration
                )
            ),
            messages = listOf(
                Message(
                    id = "1",
                    senderID = "1209", // Corresponds to Jordan Bells
                    groupID = "147", // Corresponds to this group
                    videoImage = createSampleBitmap(Color.RED),
                    videoID = "Epic Trails in Colorado"
                ),
                Message(
                    id = "2",
                    senderID = "3472", // Corresponds to Max Walker
                    groupID = "147", // Corresponds to this group
                    videoImage = createSampleBitmap(Color.YELLOW),
                    videoID = "Best Bikes for Mountain Trails"
                ),
                Message(
                    id = "3",
                    senderID = "6540", // Corresponds to Jade Chan
                    groupID = "147", // Corresponds to this group
                    videoImage = null,
                    videoID = "Safety Tips for Biking"
                ),
                Message(
                    id = "4",
                    senderID = "8921", // Corresponds to David Lee
                    groupID = "147", // Corresponds to this group
                    videoImage = createSampleBitmap(Color.BLUE),
                    videoID = "Baking Energy Bars for Rides"
                ),
                Message(
                    id = "5",
                    senderID = "1209", // Corresponds to Jordan Bells
                    groupID = "147", // Corresponds to this group
                    videoImage = createSampleBitmap(Color.RED),
                    videoID = "My Favorite Biking Gear"
                )
            )
        )
        _groups.value = fakeGroups
    }

    fun updateProfile(profile: UserProfile) {
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
