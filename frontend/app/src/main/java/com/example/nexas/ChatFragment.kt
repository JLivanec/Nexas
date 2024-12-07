package com.example.nexas

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import com.bumptech.glide.Glide
import com.example.nexas.databinding.FragmentChatBinding
import com.example.nexas.model.Group
import com.example.nexas.model.Message
import com.example.nexas.network.RetrofitClient
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class ChatFragment : Fragment(), View.OnClickListener {
    // view binding
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // Message Types
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    // UI elements
    private lateinit var backButton: ImageButton
    private lateinit var groupHeader: LinearLayout
    private lateinit var recordButton: ShapeableImageView

    private lateinit var messageRecycler: RecyclerView
    private lateinit var adapter: ChatAdapter

    private lateinit var groupId: String
    private lateinit var group: Group

    private var cameraPermissionForScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString("groupId")?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root

        backButton = binding.backButton
        groupHeader = binding.groupHeader
        recordButton = binding.recordButton

        backButton.setOnClickListener(this)
        groupHeader.setOnClickListener(this)
        recordButton.setOnClickListener(this)

        if (model.findMyGroupById(groupId) == null) {
            Toast.makeText(context, "Error: Group not found", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_chatFragment_to_groupsFragment)
        }

        group = model.findMyGroupById(groupId)!!

        // Set header
        if (group.avatar.isNotBlank()) {
            Glide.with(requireContext())
                .load(group.avatar)
                .placeholder(R.drawable.account)
                .error(R.drawable.account)
                .into(binding.groupAvatar)
        } else
            binding.groupAvatar.setImageResource(R.drawable.account)
        binding.groupName.text = group.name

        Log.d("Message", group.messages.toString())

        // Chat Recycler Setup
        messageRecycler = binding.messageRecycler
        adapter = ChatAdapter()
        messageRecycler.layoutManager = LinearLayoutManager(requireContext())
        messageRecycler.adapter = adapter
        messageRecycler.scrollToPosition(adapter.itemCount - 1)

        return view
    }



    // Handles onClick Events
    override fun onClick(v: View?) {
        when (v?.id) {
            backButton.id -> {findNavController().navigate(R.id.action_chatFragment_to_groupsFragment)}
            groupHeader.id -> {findNavController().navigate(ChatFragmentDirections.actionChatFragmentToGroupProfileFragment(groupId))}
            recordButton.id -> showRecordOptionDialog()
        }
    }

    private fun showRecordOptionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Recording Option")
            .setItems(arrayOf("Record with Screen", "Record without Screen")) { _, which ->
                when (which) {
                    0 -> checkCameraPermission(true) // Navigate to RecordFragment
                    1 -> checkCameraPermission(false) // Navigate to PrivacyVideoFragment
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.avatar)
        private val video: ImageView = itemView.findViewById(R.id.video)
        private val transcript: Button = itemView.findViewById(R.id.transcript)

        private val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        private val heartButton: ImageView = itemView.findViewById(R.id.heartButton)
        private val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        private val heartLayout: View = itemView.findViewById(R.id.heartLayout)


        fun bind(message: Message) {
            itemView.setOnClickListener {
                findNavController().navigate(ChatFragmentDirections.actionChatFragmentToWatchFragment(
                    videoImageURL = message.videoImage,
                    videoURL = message.video
                ))
            }
            val member = group.members?.find { it.id == message.senderID }
            if (member != null) {
                if (member.avatar.isNotBlank()) {
                    Glide.with(itemView.context)
                        .load(member.avatar)
                        .placeholder(R.drawable.account)
                        .error(R.drawable.account)
                        .into(avatar)
                } else
                    avatar.setImageResource(R.drawable.account)
            }
            else
                avatar.setImageResource(R.drawable.account)

            if (message.videoImage.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(message.videoImage)
                    .placeholder(R.drawable.account)
                    .error(R.drawable.account)
                    .into(video)
            } else
                video.setImageResource(R.drawable.account)

            var isLikedByCurrentUser = message.likedBy.contains(model.myProfile.id)

            likeCount.text = "${message.likedBy.size}"
            heartButton.setImageResource(
                if (isLikedByCurrentUser) R.drawable.ic_heart_checked else R.drawable.ic_heart
            )

            heartLayout.setOnClickListener {
                if (isLikedByCurrentUser) {
                    // Unlike the message
                    model.unlikeMessage(groupId, message.id)
                    message.likedBy.remove(model.myProfile.id)
                    isLikedByCurrentUser = false
                    Log.d("ChatViewHolder", "Unliked UserID: ${model.myProfile.id}")
                } else {
                    // Like the message
                    model.likeMessage(groupId, message.id)
                    message.likedBy.add(model.myProfile.id)
                    isLikedByCurrentUser = true
                    Log.d("ChatViewHolder", "liked UserID: ${model.myProfile.id}")
                }
                Log.d("ChatViewHolder", "Updated likedBy List: ${message.likedBy}")

                likeCount.text = "${message.likedBy.size}"
                heartButton.setImageResource(
                    if (message.likedBy.contains(model.myProfile.id)) R.drawable.ic_heart_checked else R.drawable.ic_heart
                )
            }

            transcript.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    transcript.isEnabled = false
                    itemView.isEnabled = false
                    val transcription = getTranscription(message)
                    Log.d("Transcript", "Transcription: $transcription")
                    builder
                        .setTitle("Transcription")
                        .setMessage(transcription)
                        .setPositiveButton("Close") {_, _ ->
                            transcript.isEnabled = true
                            itemView.isEnabled = true
                        }
                        .show()
                }
            }

        }
    }
    
    suspend fun getTranscription(message: Message): String = withContext(Dispatchers.IO) {
//        if (message.transcription.isNotEmpty()) {
//            Log.d("Transcript", "Already exists: ${message.transcription}")
//            return@withContext message.transcription
//        }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl(message.video)

        val videoFile = File.createTempFile(message.id, ".mp4")
        val audioFile = File.createTempFile(message.id, ".mp3")

        // Download the video file
        storageRef.downloadToFile(videoFile)
        val videoFilePath = videoFile.absolutePath

        // Convert video to audio
        convertVideoToAudio(videoFilePath, audioFile)

        // Transcribe the audio file
        transcribeAudioFile(audioFile)
    }

    private suspend fun StorageReference.downloadToFile(file: File): Unit = suspendCancellableCoroutine { cont ->
        getFile(file)
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private suspend fun convertVideoToAudio(videoPath: String, audioFile: File): Unit = suspendCancellableCoroutine { cont ->
        val command = arrayOf("-y", "-i", videoPath, "-q:a", "0", "-map", "a", audioFile.absolutePath)
        FFmpegKit.executeAsync(command.joinToString(" ")) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Log.d("Transcript", "Audio conversion successful")
                cont.resume(Unit)
            } else {
                Log.d("Transcript", "Audio conversion failed")
                cont.resumeWithException(RuntimeException("FFmpeg conversion failed"))
            }
        }
    }

    private suspend fun transcribeAudioFile(mp3File: File): String {
        val fileRequestBody = RequestBody.create("audio/mpeg".toMediaTypeOrNull(), mp3File)
        val filePart = MultipartBody.Part.createFormData("file", mp3File.name, fileRequestBody)
        val modelRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "whisper-1")

        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.openAIService.transcribeAudio(filePart, modelRequestBody)
                if (response.isSuccessful) {
                    response.body()?.text ?: "Empty transcription"
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("Transcript", "Error: ${response.code()} - $errorBody")
                    "Error getting transcript"
                }
            } catch (e: Exception) {
                Log.d("Transcript", "Error: ${e.message}")
                "Error getting transcript"
            }
        }
    }


    // Group Adapter
    inner class ChatAdapter : RecyclerView.Adapter<ChatViewHolder>() {
        private var messages = mutableListOf<Message>()
        init {
            messages = group.messages?.toMutableList() ?: mutableListOf<Message>()
            Log.d("Messages", messages.toString())
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val layoutId = when (viewType) {
                VIEW_TYPE_SENT -> R.layout.sent_message
                VIEW_TYPE_RECEIVED -> R.layout.received_message
                else -> throw IllegalArgumentException("Invalid view type")
            }
            val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return ChatViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            holder.bind(messages[position])
        }

        override fun getItemCount(): Int {
            return messages.size
        }

        override fun getItemViewType(position: Int): Int {
            val message = messages[position]
            return if (message.senderID == model.myProfile.id) {
                VIEW_TYPE_SENT
            } else {
                VIEW_TYPE_RECEIVED
            }
        }

    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission granted, navigate to RecordFragment
            findNavController().navigate(ChatFragmentDirections.actionChatFragmentToRecordFragment(groupId))
        } else {
            // Permission denied, show a message
            Toast.makeText(requireContext(), "Camera permission is required to record video.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermission(forScreen: Boolean) {
        cameraPermissionForScreen = forScreen
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission using the launcher
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // Permission is already granted, navigate to the appropriate fragment
            navigateToRecordingFragment(forScreen)
        }
    }

    private fun navigateToRecordingFragment(forScreen: Boolean) {
        if (forScreen) {
            findNavController().navigate(ChatFragmentDirections.actionChatFragmentToRecordFragment(groupId))
        } else {
            findNavController().navigate(ChatFragmentDirections.actionChatFragmentToPrivacyVideoFragment(groupId))
        }
    }

}