package com.example.nexas

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
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
import com.example.nexas.data.CustomLocation
import com.example.nexas.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

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

    // ViewModel
    private val model: ViewModel by activityViewModels()

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity());

        homeButton.setOnClickListener(this)
        myProfileButton.setOnClickListener(this)
        groupsButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        createButton.setOnClickListener(this)

        getLastLocation()

        return view
    }

    // Gets the location of user when they login and updates their profile
    private fun getLastLocation() {

        if (ContextCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationProviderClient!!.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Use Geocoder to get detailed location information
                        try {
                            val locationData = CustomLocation(location.latitude, location.longitude)
                            val locationString = Json.encodeToString(locationData)
                            val newProfile = model.myProfile.copy(location = locationString)

                            CoroutineScope(Dispatchers.Main).launch {
                                model.updateProfile(newProfile)
                                model.setAddress(requireContext())
                                Log.d("LOCATION", model.address.toString())
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                .addOnFailureListener {e ->
                    Log.e("LocationError", "Error getting location.", e)
                }
        }
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