package com.example.nexas
import com.example.nexas.model.*
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
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentCreateAccountBinding
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch


class CreateAccountFragment : Fragment(), View.OnClickListener {

    // View binding
    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val model: ViewModel by activityViewModels()

    // UI elements
    private lateinit var backButton: ImageButton
    private lateinit var accCreateButton: Button
    private lateinit var firstnameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        val view = binding.root

        backButton = binding.backButton
        accCreateButton = binding.accCreateButton
        firstnameInput = binding.firstnameInput
        lastNameInput = binding.lastNameInput
        usernameInput = binding.usernameInput
        emailInput = binding.emailInput
        passwordInput = binding.passwordInput

        backButton.setOnClickListener(this)
        accCreateButton.setOnClickListener(this)

        return view
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            accCreateButton.id -> {createAccount()}
            backButton.id -> {findNavController().navigateUp()}
        }
    }

    private fun createAccount() {
        if (passwordInput.text.toString() == "") {
            Toast.makeText(context, "Error: Invalid Password", Toast.LENGTH_SHORT).show()
            return
        }

        val newProfile = Profile(
            id = "",
            username = usernameInput.text.toString(),
            firstName = firstnameInput.text.toString(),
            lastName = lastNameInput.text.toString(),
            location = GeoPoint(0.0, 0.0),
            description = "",
            avatar = "",
            background = "",
            age = -1
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val error = model.createAccount(emailInput.text.toString(), passwordInput.text.toString(), newProfile)

            if (error == "") {
                Toast.makeText(context, "Account Created", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_createAccountFragment_to_homeFragment)
            }
            else
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
