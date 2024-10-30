package com.example.nexas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nexas.databinding.FragmentCreateAccountBinding

class CreateAccountFragment : Fragment() {

    // View binding
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)

        // Set up UI elements using binding
        binding.accCreateButton.setOnClickListener {
            createAccount()
        }

        return binding.root
    }

    private fun createAccount() {
        // Use binding to access views

        // TODO: Implement account creation logic
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
