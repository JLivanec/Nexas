package com.example.nexas

import android.app.AlertDialog
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexas.databinding.FragmentGroupsBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GroupsFragment : Fragment(), View.OnClickListener {
    // view binding
    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!

    // UI elements
    private lateinit var homeButton: LinearLayout
    private lateinit var myProfileButton: LinearLayout
    private lateinit var groupsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var createButton: FloatingActionButton

    private lateinit var groupsRecycler: RecyclerView
    private lateinit var adapter: GroupAdapter

    //TODO: Remove temporary array
    private var tempGroups: List<Group> = List(5) {
        Group(
            id = "${it + 1}",
            name = "Group ${it + 1}",
            avatar = null,
            location = "Location ${it + 1}",
            description = "Temp description",
            membersLimit = 10,
            members = listOf(
                Member(
                    id = "1",
                    name = "Alice",
                    avatar = null,
                    location = "New York",
                    description = "Loves hiking",
                    background = null
                ),
                Member(
                    id = "2",
                    name = "Bob",
                    avatar = null,
                    location = "San Francisco",
                    description = "Tech lover",
                    background = null
                )
            ),
            messages = listOf()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)
        val view = binding.root

        // Tabs Setup
        homeButton = binding.tabs.homeButton
        myProfileButton = binding.tabs.myProfileButton
        groupsButton = binding.tabs.groupsButton
        settingsButton = binding.tabs.settingsButton
        createButton = binding.createButton

        val groupsImage = groupsButton.findViewById<ImageView>(R.id.groupsImage)
        val groupsText = groupsButton.findViewById<TextView>(R.id.groupsText)

        groupsImage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.mint), PorterDuff.Mode.SRC_IN)
        groupsText.setTextColor(ContextCompat.getColor(requireContext(), R.color.mint))

        homeButton.setOnClickListener(this)
        myProfileButton.setOnClickListener(this)
        groupsButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        createButton.setOnClickListener(this)

        // Groups Recycler Setup
        groupsRecycler = binding.groupsRecycler
        adapter = GroupAdapter()
        groupsRecycler.layoutManager = LinearLayoutManager(requireContext())
        groupsRecycler.adapter = adapter


        return view
    }

    // Handles onClick Events
    override fun onClick(v: View?) {
        when (v?.id) {
            homeButton.id -> {findNavController().navigate(R.id.action_groupsFragment_to_homeFragment)}
            myProfileButton.id -> {findNavController().navigate(R.id.action_groupsFragment_to_myProfileFragment)}
            groupsButton.id -> {}
            settingsButton.id -> {findNavController().navigate(R.id.action_groupsFragment_to_settingsFragment)}
            createButton.id -> {findNavController().navigate(R.id.action_groupsFragment_to_createGroupFragment)}
        }
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var groupImage: ImageView = itemView.findViewById(R.id.groupImage)
        private val groupName: TextView = itemView.findViewById(R.id.groupName)
        private val groupLocation: TextView = itemView.findViewById(R.id.groupLocation)
        private val groupMembersLimit: TextView = itemView.findViewById(R.id.groupMembers)


        init {
            itemView.setOnClickListener {
                // TODO: Change location to chat screen and pass group to move to
                findNavController().navigate(R.id.action_groupsFragment_to_chatFragment)
            }
        }

        fun bind(group: Group) {
//            groupImage.setImageBitmap(group.avatar)
            groupName.text = group.name
            groupLocation.text = group.location
            groupMembersLimit.text = "${group.members.size}/${group.membersLimit}"
        }
    }

    // Group Adapter
    inner class GroupAdapter : RecyclerView.Adapter<GroupViewHolder>() {
        private var groups = mutableListOf<Group>()
        private var searchQuery = ""

        init {
            groups = tempGroups.toMutableList()
            // TODO: Get groups from DB
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.group_card, parent, false)
            return GroupViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            holder.bind(groups[position])
        }

        override fun getItemCount(): Int {
            return groups.size
        }

        fun filterGroups(query: String) {
            this.searchQuery = query
            // TODO: Search Groups
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}