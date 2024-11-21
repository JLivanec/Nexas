package com.example.nexas

import android.graphics.PorterDuff
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import java.util.Locale

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
    private var filteredGroups = listOf<Group>()

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

        // Initialize Spinner
        val distanceInput: Spinner = binding.distanceInput
        val options = arrayOf("Online", "10mi", "25mi", "50mi", "100mi")
        val distAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, options)
        distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        distanceInput.adapter = distAdapter

        var lastQuery = ""
        searchBar.addTextChangedListener { text ->
            lastQuery = text.toString()
            adapter.filterGroups(lastQuery, distanceInput.selectedItemPosition)
        }

        distanceInput.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (model.myProfile.location == GeoPoint(0.0, 0.0)) {
                    distanceInput.setSelection(0) // Set to "Online"
                    Toast.makeText(requireContext(), "Please set your location to filter by distance", Toast.LENGTH_SHORT).show()
                    return
                }
                adapter.filterGroups(lastQuery, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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
                val group = filteredGroups[adapterPosition] // Get the selected group
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
            groupLocation.text = getLocationName(group.location.latitude, group.location.longitude)
            groupMembersLimit.text = "${group.members?.size}/${group.membersLimit}"
        }
    }

    // Group Adapter
    inner class GroupAdapter : RecyclerView.Adapter<GroupViewHolder>() {
        private var allGroups = listOf<Group>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.group_card, parent, false)
            return GroupViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            holder.bind(filteredGroups[position])
        }

        override fun getItemCount(): Int {
            return filteredGroups.size
        }

        fun updateGroups(newGroups: List<Group>) {
            allGroups = newGroups
            filteredGroups = allGroups
            notifyDataSetChanged()
        }

        fun filterGroups(query: String, distance: Int) {
            val userLocation = model.myProfile.location
            filteredGroups = allGroups.filter { group ->
                val matchesQuery = query.isEmpty() || group.name.contains(query, ignoreCase = true)
                val matchesDistance = if (userLocation != GeoPoint(0.0, 0.0)) {
                    val groupLocation = group.location
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        userLocation.latitude,
                        userLocation.longitude,
                        groupLocation.latitude,
                        groupLocation.longitude,
                        results
                    )
                    val distanceInMiles = results[0] * 0.000621371 // Convert meters to miles

                    when (distance) {
                        0 -> groupLocation == GeoPoint(0.0, 0.0) // Online
                        1 -> distanceInMiles <= 10
                        2 -> distanceInMiles <= 25
                        3 -> distanceInMiles <= 50
                        4 -> distanceInMiles <= 100
                        else -> false
                    }
                } else {
                    distance == 0 // If no location set, allow "Online" only
                }

                matchesQuery && matchesDistance
            }
            notifyDataSetChanged()
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getLocationName(latitude: Double, longitude: Double): String {
        if (latitude == 0.0 && longitude == 0.0)
            return "Online"

        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses.isNullOrEmpty())
                return "Unknown"

            val local = addresses[0].locality
            val admin = addresses[0].adminArea

            return "$local, $admin"
        } catch (e: Exception) {
            return "Unknown"
        }
    }
}