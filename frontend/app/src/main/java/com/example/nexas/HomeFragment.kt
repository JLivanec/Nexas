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
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment(), View.OnClickListener {
    // view binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // UI elements
    private lateinit var homeButton: LinearLayout
    private lateinit var myProfileButton: LinearLayout
    private lateinit var groupsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var createButton: FloatingActionButton

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

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}