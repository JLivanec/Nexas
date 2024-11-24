package com.example.nexas

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(), View.OnClickListener {
    // view binding
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var homeButton: LinearLayout
    private lateinit var myProfileButton: LinearLayout
    private lateinit var groupsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        homeButton = binding.tabs.homeButton
        myProfileButton = binding.tabs.myProfileButton
        groupsButton = binding.tabs.groupsButton
        settingsButton = binding.tabs.settingsButton
        logoutButton = binding.logoutButton

        val settingsImage = settingsButton.findViewById<ImageView>(R.id.settingsImage)
        val settingsText = settingsButton.findViewById<TextView>(R.id.settingsText)

        settingsImage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.mint), PorterDuff.Mode.SRC_IN)
        settingsText.setTextColor(ContextCompat.getColor(requireContext(), R.color.mint))

        homeButton.setOnClickListener(this)
        myProfileButton.setOnClickListener(this)
        groupsButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        logoutButton.setOnClickListener(this)

        return view
    }

    // handle click events
    override fun onClick(v: View?) {
        when (v?.id) {
            homeButton.id -> {findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)}
            myProfileButton.id -> {findNavController().navigate(R.id.action_settingsFragment_to_myProfileFragment)}
            groupsButton.id -> {findNavController().navigate(R.id.action_settingsFragment_to_groupsFragment)}
            settingsButton.id -> {}
            logoutButton.id -> {
                model.logout()
                findNavController().navigate(R.id.action_settingsFragment_to_loadingFragment)
            }
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}