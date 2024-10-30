package com.example.nexas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    // view binding
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // set click listeners for buttons
        binding.loginButton.setOnClickListener { signIn() }
        binding.createAccountButton.setOnClickListener { createAccount() }

        return binding.root
    }

    // called when user taps sign in button
    private fun signIn() {
        //TODO: Setup login
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    // called when user taps create account button
    private fun createAccount() {
        //TODO: Setup account creation
        findNavController().navigate(R.id.action_loginFragment_to_createAccountFragment)
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
