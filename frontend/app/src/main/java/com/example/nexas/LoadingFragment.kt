package com.example.nexas

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentLoadingBinding

class LoadingFragment : Fragment(), View.OnClickListener {

    // view binding
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var loginButton: Button
    private lateinit var createAccountButton: Button

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        val view = binding.root

        loginButton = binding.loginButton
        createAccountButton = binding.createAccountButton

        loginButton.setOnClickListener(this)
        createAccountButton.setOnClickListener(this)

        // TODO: Allow auto login after removing fake base profile
//        viewLifecycleOwner.lifecycleScope.launch {
//            if (model.autoLogin())
//                findNavController().navigate(R.id.action_loadingFragment_to_homeFragment)
//        }


        val isPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        Log.d("PERMISSION", isPermissionGranted.toString())
        if(!isPermissionGranted) {
            Log.d("PERMISSION", "Ask Permission")
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        return view
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, you can access location
                } else {
                    loginButton.isEnabled = false
                    createAccountButton.isEnabled = false
                    showPermissionRequiredMessage()
                }
            }
        }
    }

    // Function to show a dialog explaining why the permission is needed
    private fun showPermissionRequiredMessage() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage("Location permission is required to use this app. Please grant permission in settings.")
            .setPositiveButton("Done", null)
            .show().apply {
                findViewById<TextView>(android.R.id.title)?.setTextColor(Color.WHITE)
                findViewById<TextView>(android.R.id.message)?.setTextColor(Color.WHITE)
                getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
            }

    }

    // Handles onClick Events
    override fun onClick(v: View?) {
        when (v?.id) {
            loginButton.id -> {findNavController().navigate(R.id.action_loadingFragment_to_loginFragment)}
            createAccountButton.id -> {findNavController().navigate(R.id.action_loadingFragment_to_createAccountFragment)}
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}