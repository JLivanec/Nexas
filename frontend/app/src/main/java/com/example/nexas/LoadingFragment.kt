package com.example.nexas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        return view
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