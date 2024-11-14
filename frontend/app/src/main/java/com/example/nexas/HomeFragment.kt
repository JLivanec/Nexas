package com.example.nexas

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexas.GroupsFragment.GroupAdapter
import com.example.nexas.databinding.FragmentHomeBinding
import com.example.nexas.model.Group
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), View.OnClickListener {
    // view binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var homeButton: LinearLayout
    private lateinit var myProfileButton: LinearLayout
    private lateinit var groupsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var createButton: FloatingActionButton

    private lateinit var searchBar: EditText
    private lateinit var groupsRecycler: RecyclerView
    private lateinit var adapter: GroupAdapter
    private lateinit var groups: List<Group>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        homeButton = binding.tabs.homeButton
        myProfileButton = binding.tabs.myProfileButton
        groupsButton = binding.tabs.groupsButton
        settingsButton = binding.tabs.settingsButton
        createButton = binding.createButton

        val homeImage = homeButton.findViewById<ImageView>(R.id.homeImage)
        val homeText = homeButton.findViewById<TextView>(R.id.homeText)

        homeImage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.mint), PorterDuff.Mode.SRC_IN)
        homeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.mint))

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

        viewLifecycleOwner.lifecycleScope.launch {
            groups = model.getGroups()
            adapter.updateGroups(groups)
        }

        searchBar = binding.searchBar.searchBar

        searchBar.addTextChangedListener { text ->
            val query = text.toString()
            adapter.filterGroups(query)
        }

        return view
    }

    // handle click events
    override fun onClick(v: View?) {
        when (v?.id) {
            homeButton.id -> {}
            myProfileButton.id -> {findNavController().navigate(R.id.action_homeFragment_to_myProfileFragment)}
            groupsButton.id -> {findNavController().navigate(R.id.action_homeFragment_to_groupsFragment)}
            settingsButton.id -> {findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)}
            createButton.id -> {findNavController().navigate(R.id.action_homeFragment_to_createGroupFragment)}
        }
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var groupImage: ImageView = itemView.findViewById(R.id.groupImage)
        private val groupName: TextView = itemView.findViewById(R.id.groupName)
        private val groupLocation: TextView = itemView.findViewById(R.id.groupLocation)
        private val groupMembersLimit: TextView = itemView.findViewById(R.id.groupMembers)


        init {
            itemView.setOnClickListener {
                val group = groups[adapterPosition] // Get the selected group
                val action = HomeFragmentDirections.actionHomeFragmentToGroupProfileFragment(group.id)
                findNavController().navigate(action)
            }
        }

        fun bind(group: Group) {
            if (group.avatar.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(group.avatar)
                    .placeholder(R.drawable.account)
                    .error(R.drawable.account)
                    .into(groupImage)
            } else
                groupImage.setImageResource(R.drawable.account)
            groupName.text = group.name
            groupLocation.text = group.location
            groupMembersLimit.text = "${group.members?.size}/${group.membersLimit}"
        }
    }

    // Group Adapter
    inner class GroupAdapter : RecyclerView.Adapter<GroupViewHolder>() {
        private var groups = listOf<Group>()
        private var allGroups = listOf<Group>()

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

        fun updateGroups(newGroups: List<Group>) {
            allGroups = newGroups
            groups = allGroups
            notifyDataSetChanged()
        }

        fun filterGroups(query: String) {
            if (query.isEmpty())
                groups = allGroups
            else
                groups = allGroups.filter { it.name.contains(query, ignoreCase = true) }
            notifyDataSetChanged()
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}