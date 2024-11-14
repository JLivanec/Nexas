package com.example.nexas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexas.GroupsFragment.GroupAdapter
import com.example.nexas.databinding.FragmentGroupProfileBinding
import com.example.nexas.model.Group
import com.example.nexas.model.Profile
import kotlinx.coroutines.launch

class GroupProfileFragment : Fragment(), View.OnClickListener {

    // view binding
    private var _binding: FragmentGroupProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var backButton: ImageButton
    private lateinit var joinLeaveButton: Button


    private lateinit var groupId: String
    private lateinit var group: Group

    private lateinit var profilesRecycler: RecyclerView
    private lateinit var adapter: ProfileAdapter
    private lateinit var profiles: List<Profile>

    private var isMember = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString("groupId")?: ""
        }
        if (model.findGroupById(groupId) == null) {
            Toast.makeText(context, "Error: Group not found", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_groupProfileFragment_to_homeFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        backButton = binding.backButton
        joinLeaveButton = binding.joinLeaveButton

        backButton.setOnClickListener(this)
        joinLeaveButton.setOnClickListener(this)

        profilesRecycler = binding.profilesRecycler
        adapter = ProfileAdapter()
        profilesRecycler.layoutManager = LinearLayoutManager(requireContext())
        profilesRecycler.adapter = adapter

        updateGroupScreen()

        return view
    }

    fun updateGroupScreen() {
        if (model.findGroupById(groupId) == null) {
            Toast.makeText(context, "Error: Group not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        group = model.findGroupById(groupId)!!
        profiles = group.members!!
        isMember = profiles.any { it.id == model.myProfile.id }

        if (group.avatar.isNotBlank()) {
            Glide.with(requireContext())
                .load(group.avatar)
                .placeholder(R.drawable.account)
                .error(R.drawable.account)
                .into(binding.groupAvatar)
        } else
            binding.groupAvatar.setImageResource(R.drawable.account)
        binding.groupName.text = group.name
        binding.descriptionText.text = group.description
        binding.membersTitle.text = "Members (${group.members?.size}/${group.membersLimit})"
        group.members?.let { adapter.updateProfiles(it) }

        if (isMember) {
            binding.joinLeaveButton.text = "Leave Group"
            binding.joinLeaveButton.setBackgroundColor(resources.getColor(R.color.red))
        } else {
            binding.joinLeaveButton.text = "Join Group"
            binding.joinLeaveButton.setBackgroundColor(resources.getColor(R.color.mint))
        }
    }

    // Handles onClick Events
    override fun onClick(v: View?) {
        when (v?.id) {
            backButton.id -> {findNavController().navigateUp()}
            joinLeaveButton.id -> {
                if (isMember){
                    viewLifecycleOwner.lifecycleScope.launch {
                        model.leaveGroup(groupId)
                        updateGroupScreen()
                    }
                } else {
                    viewLifecycleOwner.lifecycleScope.launch {
                        model.joinGroup(groupId)
                        updateGroupScreen()
                    }
                }
            }
        }
    }

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var profileImage: ImageView = itemView.findViewById(R.id.profileAvatar)
        private val profileName: TextView = itemView.findViewById(R.id.profileName)


        init {
            itemView.setOnClickListener {
                val profile = profiles[adapterPosition] // Get the selected profile
                val action = GroupProfileFragmentDirections.actionGroupProfileFragmentToProfileFragment(profile.id)
                findNavController().navigate(action)
            }
        }

        fun bind(profile: Profile) {
            if (profile.avatar.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(profile.avatar)
                    .placeholder(R.drawable.account)
                    .error(R.drawable.account)
                    .into(profileImage)
            } else
                profileImage.setImageResource(R.drawable.account)
            profileName.text = "${profile.firstName} ${profile.lastName}"
        }
    }

    // Profile Adapter
    inner class ProfileAdapter : RecyclerView.Adapter<ProfileViewHolder>() {
        private var profiles = mutableListOf<Profile>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.profile_card, parent, false)
            return ProfileViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
            holder.bind(profiles[position])
        }

        fun updateProfiles(newProfiles: List<Profile>) {
            this.profiles.clear()
            this.profiles.addAll(newProfiles)
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return profiles.size
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}