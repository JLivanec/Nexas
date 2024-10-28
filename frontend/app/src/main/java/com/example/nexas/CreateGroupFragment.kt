package com.example.nexas

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentCreateGroupBinding


class CreateGroupFragment : Fragment(), View.OnClickListener {
    // view binding
    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!

    // UI elements
    private lateinit var homeButton: LinearLayout
    private lateinit var myProfileButton: LinearLayout
    private lateinit var groupsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var backButton: ImageButton
    private lateinit var submitButton: Button

    private lateinit var groupNameInput: EditText
    private lateinit var groupDescriptionInput: EditText
    private lateinit var maxMembersSpinner: Spinner

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

        homeButton.setOnClickListener(this)
        myProfileButton.setOnClickListener(this)
        groupsButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        backButton.setOnClickListener(this)
        submitButton.setOnClickListener(this)

        groupNameInput = binding.groupNameInput
        groupDescriptionInput = binding.groupDescriptionInput
        maxMembersSpinner = binding.maxMembersSpinner

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
                val view = super.getView(position, convertView, parent)
                (view as TextView).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.TRANSPARENT)
                    gravity = Gravity.CENTER
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.TRANSPARENT)
                    gravity = Gravity.CENTER
                }
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        maxMembersSpinner.adapter = adapter

        return view
    }

    // Create Group
    private fun createGroup() {
        val groupName = groupNameInput.text.toString()
        val groupDescription = groupDescriptionInput.text.toString()
        val maxMembers = maxMembersSpinner.selectedItem.toString().toInt()

        // TODO: Create group
        findNavController().navigateUp()
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
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}