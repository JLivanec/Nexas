package com.example.nexas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexas.databinding.FragmentChatBinding
import com.google.android.material.imageview.ShapeableImageView
import com.example.nexas.model.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString("groupId")?: ""
        }
        if (model.findMyGroupById(groupId) == null) {
            Toast.makeText(context, "Error: Group not found", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_chatFragment_to_groupsFragment)
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
            backButton.id -> {findNavController().navigateUp()}
            groupHeader.id -> {findNavController().navigate(ChatFragmentDirections.actionChatFragmentToGroupProfileFragment(groupId))}
            recordButton.id -> checkCameraPermission()
        }
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.avatar)
        private val video: ImageView = itemView.findViewById(R.id.video)


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
        }
    }

    // Group Adapter
    inner class ChatAdapter : RecyclerView.Adapter<ChatViewHolder>() {
        private var messages = mutableListOf<Message>()

        init {
            messages = group.messages?.toMutableList() ?: mutableListOf<Message>()
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

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission using the launcher
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // Permission is already granted, navigate to RecordFragment
            findNavController().navigate(ChatFragmentDirections.actionChatFragmentToRecordFragment(groupId))
        }
    }

}