package com.example.nexas

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentLoginBinding

class LoginFragment : Fragment(), View.OnClickListener {
    // view binding
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // UI elements
    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        // grab a handle to UI elements
        loginButton = binding.loginButton

        // register to onClick method
        loginButton.setOnClickListener(this)

        return view
    }

    // handle click events
    override fun onClick(v: View?) {
        when (v?.id) {
            loginButton.id -> signIn()
        }
    }

    // called when user taps sign in button
    private fun signIn() {
        //TODO: Setup login
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}