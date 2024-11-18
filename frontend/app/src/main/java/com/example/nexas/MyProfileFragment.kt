package com.example.nexas

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.nexas.databinding.FragmentMyProfileBinding
import com.example.nexas.model.Profile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch


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
    private lateinit var profileImage: ShapeableImageView
    private lateinit var profileFirstName: EditText
    private lateinit var profileLastName: EditText
    private lateinit var profileLocation: EditText
    private lateinit var bioText: EditText
    private lateinit var backgroundImage: ShapeableImageView
    private lateinit var editButton: FloatingActionButton
    private lateinit var cancelButton: FloatingActionButton


    // Constant for image picking
    private val IMAGE_PICK_CODE = 1000

    private var editing = false
    private var curAvatarURI: String? = null
    private var curBackgroundURI: String? = null

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private val REQUEST_CODE: Int = 100

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
        profileImage = binding.profileImage
        profileFirstName = binding.profileFirstName
        profileLastName = binding.profileLastName
        profileLocation = binding.profileLocation
        bioText = binding.bioText
        backgroundImage = binding.backgroundImage
        editButton = binding.editButton
        cancelButton = binding.cancelButton

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity());

        val myProfileImage = myProfileButton.findViewById<ImageView>(R.id.myProfileImage)
        val myProfileText = myProfileButton.findViewById<TextView>(R.id.myProfileText)

        myProfileImage.setColorFilter(ContextCompat.getColor(requireContext(), R.color.mint), PorterDuff.Mode.SRC_IN)
        myProfileText.setTextColor(ContextCompat.getColor(requireContext(), R.color.mint))

        homeButton.setOnClickListener(this)
        groupsButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        profileImage.setOnClickListener(this)
        backgroundImage.setOnClickListener(this)
        editButton.setOnClickListener(this)
        cancelButton.setOnClickListener(this)

        stopEditing()
        updateView()

        return view
    }


    private fun updateView() {
        if (model.myProfile.avatar != "") {
            if (isValidUrl(model.myProfile.avatar)) {
                Glide.with(this)
                    .load(model.myProfile.avatar)
                    .into(profileImage)
                profileImage.setBackgroundColor(Color.TRANSPARENT)
            }
            else if(isValidUri(model.myProfile.avatar)) {
                profileImage.setImageURI(model.myProfile.avatar.toUri())
                profileImage.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        else if (curAvatarURI != null)
            profileImage.setImageURI(curAvatarURI!!.toUri())
        else {
            profileImage.setImageResource(R.drawable.profile)
            profileImage.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.box_background))
        }

        profileFirstName.setText(model.myProfile.firstName)
        profileLastName.setText(model.myProfile.lastName)
        profileLocation.setText(getString(R.string.profile_location, model.locality, model.state))
        bioText.setText(model.myProfile.description)

        if (model.myProfile.background != "") {
            if (isValidUrl(model.myProfile.avatar))
                Glide.with(this)
                    .load(model.myProfile.background)
                    .into(backgroundImage)
            else if (isValidUri(model.myProfile.avatar))
                backgroundImage.setImageURI(model.myProfile.background.toUri())
        }
        else if (curBackgroundURI != null)
            backgroundImage.setImageURI(curBackgroundURI!!.toUri())
        else
            backgroundImage.setImageResource(R.drawable.jordan_background)
    }

    // handle click events
    override fun onClick(v: View?) {
        when (v?.id) {
            homeButton.id -> {findNavController().navigate(R.id.action_myProfileFragment_to_homeFragment)}
            groupsButton.id -> {findNavController().navigate(R.id.action_myProfileFragment_to_groupsFragment)}
            settingsButton.id -> {findNavController().navigate(R.id.action_myProfileFragment_to_settingsFragment)}
            profileImage.id -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                startActivityForResult(intent, IMAGE_PICK_CODE)
            }
            backgroundImage.id -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                startActivityForResult(intent, IMAGE_PICK_CODE + 1) // Use a different request code
            }
            editButton.id -> {
                if (editing) {
                    stopEditing()
                    viewLifecycleOwner.lifecycleScope.launch {
                        val error = model.updateProfile(
                            Profile(
                                id = model.myProfile.id,
                                avatar = curAvatarURI ?: model.myProfile.avatar,
                                username = model.myProfile.username,
                                firstName = profileFirstName.text.toString(),
                                lastName = profileLastName.text.toString(),
                                location = profileLocation.text.toString(),
                                description = bioText.text.toString(),
                                background = curBackgroundURI ?: model.myProfile.background,
                                age = model.myProfile.age
                            )
                        )

                        if (error == "")
                            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                        updateView()
                    }
                }
                else {
                    allowEditing()
                }
            }
            cancelButton.id -> {
                curAvatarURI = null
                curBackgroundURI = null
                stopEditing()
                updateView()
            }
        }
    }

    private fun allowEditing() {
        editButton.setImageResource(R.drawable.check)
        profileImage.isClickable = true
        backgroundImage.isClickable = true

        // Set EditTexts to be non-editable
        profileFirstName.isClickable = true
        profileFirstName.isFocusable = true
        profileFirstName.isFocusableInTouchMode = true

        profileLastName.isClickable = true
        profileLastName.isFocusable = true
        profileLastName.isFocusableInTouchMode = true

        profileLocation.isClickable = true
        profileLocation.isFocusable = true
        profileLocation.isFocusableInTouchMode = true

        bioText.isClickable = true
        bioText.isFocusable = true
        bioText.isFocusableInTouchMode = true

        cancelButton.show()
        editing = true
    }

    private fun stopEditing() {
        editButton.setImageResource(R.drawable.edit)
        profileImage.isClickable = false
        backgroundImage.isClickable = false

        // Set EditTexts to be non-editable
        profileFirstName.isClickable = false
        profileFirstName.isFocusable = false
        profileFirstName.isFocusableInTouchMode = false

        profileLastName.isClickable = false
        profileLastName.isFocusable = false
        profileLastName.isFocusableInTouchMode = false

        profileLocation.isClickable = false
        profileLocation.isFocusable = false
        profileLocation.isFocusableInTouchMode = false

        bioText.isClickable = false
        bioText.isFocusable = false
        bioText.isFocusableInTouchMode = false


        cancelButton.hide()
        editing = false
    }

    // Handle the result from image picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    profileImage.setImageURI(imageUri)
                    curAvatarURI = imageUri.toString()
                }
                IMAGE_PICK_CODE + 1 -> { // Handle background image selection
                    backgroundImage.setImageURI(imageUri)
                    curBackgroundURI = imageUri.toString()
                }
            }
        }
    }

    private fun isValidUri(input: String?): Boolean {
        try {
            val uri = Uri.parse(input)
            return uri.scheme != null
        } catch (e: java.lang.Exception) {
            return false
        }
    }

    private fun isValidUrl(input: String?): Boolean {
        try {
            val uri = Uri.parse(input)
            return uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https")
        } catch (e: java.lang.Exception) {
            return false
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}