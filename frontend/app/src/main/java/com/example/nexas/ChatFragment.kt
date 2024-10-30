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
import com.example.nexas.databinding.FragmentChatBinding
import com.google.android.material.imageview.ShapeableImageView

class ChatFragment : Fragment(), View.OnClickListener {
    // View binding
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // Message types
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    // UI elements
    private lateinit var backButton: ImageButton
    private lateinit var groupHeader: LinearLayout
    private lateinit var recordButton: ShapeableImageView

    private lateinit var messageRecycler: RecyclerView
    private lateinit var adapter: ChatAdapter

    private lateinit var myID: String
    private lateinit var group: Group

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root

        // Retrieve the group ID from arguments
        val groupId = arguments?.getString("groupId") ?: return view
        myID = model.myProfile.id

        backButton = binding.backButton
        groupHeader = binding.groupHeader
        recordButton = binding.recordButton

        backButton.setOnClickListener(this)
        groupHeader.setOnClickListener(this)
        recordButton.setOnClickListener(this)

        // Chat Recycler Setup
        messageRecycler = binding.messageRecycler
        adapter = ChatAdapter()
        messageRecycler.layoutManager = LinearLayoutManager(requireContext())
        messageRecycler.adapter = adapter

        model.groups.observe(viewLifecycleOwner) { groups ->
            group = groups.find { it.id == groupId } ?: return@observe
            binding.groupAvatar.setImageBitmap(group.avatar)
            binding.groupName.text = group.name
            adapter.updateMessages(group.messages)
            messageRecycler.scrollToPosition(adapter.itemCount - 1)
        }

        return view
    }

    // Handles onClick Events
    override fun onClick(v: View?) {
        when (v?.id) {
            backButton.id -> {findNavController().navigateUp()}
            groupHeader.id -> {findNavController().navigate(R.id.action_chatFragment_to_groupProfileFragment)}
            recordButton.id -> {}
        }
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.avatar)
        private val video: ImageView = itemView.findViewById(R.id.video)

        init {
            itemView.setOnClickListener {
                // TODO: Change location to chat screen and pass group to move to
                Log.d("Chat", "Watch")
            }
        }

        fun bind(message: Message) {
            val member = group.members.find { it.id == message.senderID }

            if (member != null) {
                member.avatar?.let { avatarBitmap ->
                    avatar.setImageBitmap(avatarBitmap)
                } ?: run {
                    avatar.setImageResource(R.drawable.account)
                }
            } else {
                avatar.setImageResource(R.drawable.account)
            }

            message.videoImage?.let { videoBitmap ->
                video.setImageBitmap(videoBitmap)
                video.clearColorFilter()
            } ?: run {
                video.setImageBitmap(null)
                video.setColorFilter(ContextCompat.getColor(video.context, R.color.box_background), PorterDuff.Mode.SRC_IN)
            }
        }
    }

    // Group Adapter
    inner class ChatAdapter : RecyclerView.Adapter<ChatViewHolder>() {
        private var messages = mutableListOf<Message>()

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

        fun updateMessages(newMessages: List<Message>) {
            this.messages.clear()
            this.messages.addAll(newMessages)
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            val message = messages[position]
            return if (message.senderID == myID) {
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