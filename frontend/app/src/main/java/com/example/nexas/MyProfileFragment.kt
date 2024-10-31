package com.example.nexas

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentMyProfileBinding

class MyProfileFragment : Fragment(), View.OnClickListener {
    // view binding
    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var homeButton: LinearLayout
    private lateinit var myProfileButton: LinearLayout
    private lateinit var groupsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        homeButton = binding.tabs.homeButton
        myProfileButton = binding.tabs.myProfileButton
        groupsButton = binding.tabs.groupsButton
        settingsButton = binding.tabs.settingsButton

        val myProfileImage = myProfileButton.findViewById<ImageView>(R.id.myProfileImage)
        val myProfileText = myProfileButton.findViewById<TextView>(R.id.myProfileText)

        myProfileImage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.mint), PorterDuff.Mode.SRC_IN)
        myProfileText.setTextColor(ContextCompat.getColor(requireContext(), R.color.mint))

        homeButton.setOnClickListener(this)
        myProfileButton.setOnClickListener(this)
        groupsButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)

        updateView()

        return view
    }

    private fun updateView() {
        binding.profileName.text = model.myProfile.fname
        binding.profileLocation.text = model.myProfile.location
        binding.bioText.text = model.myProfile.description
    }

    // handle click events
    override fun onClick(v: View?) {
        when (v?.id) {
            homeButton.id -> {findNavController().navigate(R.id.action_myProfileFragment_to_homeFragment)}
            myProfileButton.id -> {}
            groupsButton.id -> {findNavController().navigate(R.id.action_myProfileFragment_to_groupsFragment)}
            settingsButton.id -> {findNavController().navigate(R.id.action_myProfileFragment_to_settingsFragment)}
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}