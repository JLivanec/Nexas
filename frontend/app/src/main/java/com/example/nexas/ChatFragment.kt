package com.example.nexas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexas.databinding.FragmentChatBinding
import com.google.android.material.imageview.ShapeableImageView
import com.example.nexas.model.*
import java.time.Instant

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

    fun createSampleBitmap(): Bitmap {
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawColor(Color.RED)
        }
    }
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

        group = model.findGroupById(groupId)!!

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
            groupHeader.id -> {findNavController().navigate(R.id.action_chatFragment_to_groupProfileFragment)}
            recordButton.id -> {Log.d("Chat", "Record")} // TODO: Setup recording
        }
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.avatar)
        private val video: ImageView = itemView.findViewById(R.id.video)

        fun bind(message: Message) {
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

            video.setImageBitmap(null)
            video.setColorFilter(ContextCompat.getColor(video.context, R.color.box_background), PorterDuff.Mode.SRC_IN)
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
}