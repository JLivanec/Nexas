package com.example.nexas

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentCreateGroupBinding
import com.google.android.material.imageview.ShapeableImageView
import com.example.nexas.model.*
import kotlinx.coroutines.launch


class CreateGroupFragment : Fragment(), View.OnClickListener {
    // View binding
    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var homeButton: LinearLayout
    private lateinit var myProfileButton: LinearLayout
    private lateinit var groupsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var backButton: ImageButton
    private lateinit var submitButton: Button

    private lateinit var groupNameInput: EditText
    private lateinit var groupAvatarInput: ShapeableImageView
    private lateinit var groupDescriptionInput: EditText
    private lateinit var maxMembersSpinner: Spinner

    // Constant for image picking
    private val IMAGE_PICK_CODE = 1000

    private var curAvatarURI: String? = null

    private val maxMembersOptions = arrayOf(3, 5, 7, 10, 15, 20)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        val view = binding.root

        homeButton = binding.tabs.homeButton
        myProfileButton = binding.tabs.myProfileButton
        groupsButton = binding.tabs.groupsButton
        settingsButton = binding.tabs.settingsButton
        backButton = binding.backButton
        submitButton = binding.submitButton

        groupNameInput = binding.groupNameInput
        groupAvatarInput = binding.groupAvatarInput
        groupDescriptionInput = binding.groupDescriptionInput
        maxMembersSpinner = binding.maxMembersSpinner

        // Set default image in groupAvatarInput
        groupAvatarInput.setImageResource(R.drawable.groups)

        homeButton.setOnClickListener(this)
        myProfileButton.setOnClickListener(this)
        groupsButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
        submitButton.setOnClickListener(this)
        groupAvatarInput.setOnClickListener(this)

        groupNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var lastCharIndex = s.length - 1
                while (groupNameInput.layout.lineCount > 1 && lastCharIndex >= 0) {
                    val newText = s.substring(0, lastCharIndex)
                    groupNameInput.setText(newText)
                    groupNameInput.setSelection(newText.length)
                    lastCharIndex -= 1
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        groupDescriptionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var lastCharIndex = s.length - 1
                while (groupDescriptionInput.layout.lineCount > 8 && lastCharIndex >= 0) {
                    val newText = s.substring(0, lastCharIndex)
                    groupDescriptionInput.setText(newText)
                    groupDescriptionInput.setSelection(newText.length)
                    lastCharIndex -= 1
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        // Set up spinner
        val adapter = object : ArrayAdapter<Int>(requireContext(), android.R.layout.simple_spinner_item, maxMembersOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tView = super.getView(position, convertView, parent)
                (tView as TextView).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.TRANSPARENT)
                    gravity = Gravity.CENTER
                }
                return tView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tView = super.getDropDownView(position, convertView, parent)
                (tView as TextView).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.TRANSPARENT)
                    gravity = Gravity.CENTER
                }
                return tView
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        maxMembersSpinner.adapter = adapter

        return view
    }

    // Create Group
    private fun createGroup() {
        val groupName = groupNameInput.text.toString()
//        val groupAvatar = curAvatarURI?: ""
        val groupAvatar = curAvatarURI ?: Uri.parse("android.resource://${requireContext().packageName}/drawable/groups").toString()
        val groupDescription = groupDescriptionInput.text.toString()
        val maxMembers = maxMembersSpinner.selectedItem.toString().toInt()

        viewLifecycleOwner.lifecycleScope.launch {
            val error = model.createGroup(
                Group(
                    id = "",
                    name = groupName,
                    avatar = groupAvatar,
                    location = model.myProfile.location,
                    description = groupDescription,
                    membersLimit = maxMembers,
                    members = listOf(model.myProfile),
                    messages = listOf()
                )
            )
            if (error == "") {
                Toast.makeText(context, "Group created successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // handle click events
    override fun onClick(v: View?) {
        when (v?.id) {
            homeButton.id -> {findNavController().navigate(R.id.action_createGroupFragment_to_homeFragment)}
            myProfileButton.id -> {findNavController().navigate(R.id.action_createGroupFragment_to_myProfileFragment)}
            groupsButton.id -> {findNavController().navigate(R.id.action_createGroupFragment_to_groupsFragment)}
            settingsButton.id -> {findNavController().navigate(R.id.action_createGroupFragment_to_settingsFragment)}
            backButton.id -> {findNavController().navigateUp()}
            submitButton.id -> {createGroup()}
            groupAvatarInput.id -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                startActivityForResult(intent, IMAGE_PICK_CODE)
            }
        }
    }

    // Handle the result from image picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (requestCode == IMAGE_PICK_CODE) {
                    groupAvatarInput.setImageURI(imageUri)
                    curAvatarURI = imageUri.toString()
            }
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}