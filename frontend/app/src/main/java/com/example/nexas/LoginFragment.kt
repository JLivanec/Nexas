package com.example.nexas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nexas.databinding.FragmentLoginBinding
import com.example.nexas.model.UserProfile
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class LoginFragment : Fragment(), View.OnClickListener {

    // view binding
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var backButton: ImageButton
    private lateinit var loginButton: Button
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        backButton = binding.backButton
        loginButton = binding.loginButton
        usernameInput = binding.usernameInput
        passwordInput = binding.passwordInput

        backButton.setOnClickListener(this)
        loginButton.setOnClickListener(this)

        return binding.root
    }

    // Handles onClick Events
    override fun onClick(v: View?) {
        when (v?.id) {
            backButton.id -> {findNavController().navigateUp()}
            loginButton.id -> {login()}
        }
    }

    // Handles login functionality
    private fun login() {
        viewLifecycleOwner.lifecycleScope.launch {
            val error = model.login(
                usernameInput.text.toString(),
                BCrypt.hashpw(passwordInput.text.toString(), BCrypt.gensalt())
            )

            if (error == "") {
                Toast.makeText(context, "Logged In", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
