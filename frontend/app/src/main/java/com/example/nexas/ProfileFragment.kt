package com.example.nexas

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexas.GroupProfileFragment.ProfileAdapter
import com.example.nexas.databinding.FragmentProfileBinding
import com.example.nexas.model.Profile
import com.google.android.material.imageview.ShapeableImageView

class ProfileFragment : Fragment(), View.OnClickListener {

    // view binding
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var homeButton: LinearLayout
    private lateinit var myProfileButton: LinearLayout
    private lateinit var groupsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var backButton: ImageView
    private lateinit var profileImage: ShapeableImageView
    private lateinit var profileFirstName: TextView
    private lateinit var profileLastName: TextView
    private lateinit var profileLocation: TextView
    private lateinit var bioText: TextView
    private lateinit var backgroundImage: ShapeableImageView


    private lateinit var profileId: String
    private lateinit var profile: Profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profileId = it.getString("profileId") ?: ""
        }
        if (model.findProfileById(profileId) == null) {
            Toast.makeText(context, "Error: Profile not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        profile = model.findProfileById(profileId)!!

        homeButton = binding.tabs.homeButton
        myProfileButton = binding.tabs.myProfileButton
        groupsButton = binding.tabs.groupsButton
        settingsButton = binding.tabs.settingsButton
        backButton = binding.backButton
        profileImage = binding.profileImage
        profileFirstName = binding.profileFirstName
        profileLastName = binding.profileLastName
        profileLocation = binding.profileLocation
        bioText = binding.bioText
        backgroundImage = binding.backgroundImage

        homeButton.setOnClickListener(this)
        myProfileButton.setOnClickListener(this)
        groupsButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        backButton.setOnClickListener(this)

        if (profile.avatar != "") {
            Glide.with(this)
                .load(profile.avatar)
                .into(profileImage)
            profileImage.setBackgroundColor(Color.TRANSPARENT)
        }
        else {
            profileImage.setImageResource(R.drawable.profile)
            profileImage.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.box_background))
        }

        profileFirstName.text = profile.firstName
        profileLastName.text = profile.lastName
        profileLocation.text = profile.location
        bioText.text = profile.description

        if (profile.background != "") {
            Glide.with(this)
                .load(profile.background)
                .into(backgroundImage)
        }
        else
            backgroundImage.setImageResource(R.drawable.jordan_background)

        return view
    }

    // handle click events
    override fun onClick(v: View?) {
        when (v?.id) {
            homeButton.id -> {findNavController().navigate(R.id.action_profileFragment_to_homeFragment)}
            myProfileButton.id -> {findNavController().navigate(R.id.action_profileFragment_to_myProfileFragment)}
            groupsButton.id -> {findNavController().navigate(R.id.action_profileFragment_to_groupsFragment)}
            settingsButton.id -> {findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)}
            backButton.id -> {findNavController().navigateUp()}
        }
    }
}
