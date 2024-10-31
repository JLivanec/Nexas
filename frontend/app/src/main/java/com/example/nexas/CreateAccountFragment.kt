package com.example.nexas
import com.example.nexas.data.MongoClientConnection
import com.example.nexas.model.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentCreateAccountBinding
import com.mongodb.kotlin.client.coroutine.MongoClient
import org.mindrot.jbcrypt.BCrypt


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

        val db = MongoClientConnection()

        // Set up UI elements using binding
        binding.accCreateButton.setOnClickListener {
            var validInputs = true
            var error_msg = "Account creation error!"

            var fname = binding.firstnameInput.text.toString()
            var lname = binding.lastNameInput.text.toString()
            var uname = binding.usernameInput.text.toString()
            var eml = binding.emailInput.text.toString()
            var unhashed_pword = binding.passwordInput.text.toString()
            var pword =  BCrypt.hashpw(unhashed_pword, BCrypt.gensalt())

            //check validity of inputs
            if (fname == "" || lname == "" || uname == "" || eml == "" || unhashed_pword == "") {
                validInputs = false
                error_msg = "Please fill out all fields"
            }
            if (!eml.contains(".*@.*")) {
                validInputs = false
                error_msg = "Invalid Email Input"
            }
            //TODO: duplicate entry checks
            //if (db.getUserProfileByUsername(uname) != null) {
            //    validInputs = false
            //   error_msg = "An account with this username already exists!"
            //}
            //TODO: Prompt Age

            if (!validInputs) {
                Toast.makeText(context, error_msg, Toast.LENGTH_SHORT).show()
            } else {
                val newProfile = UserProfile(
                    uname = uname,
                    fname = fname,
                    lname = lname,
                    email = eml,
                    location = "online",
                    description = "",
                    avatar = null,
                    background = null,
                    age = 18,
                    hashedPassword = pword
                )

                createAccount(newProfile, db)
                Toast.makeText(context, "Account Created successfully", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun createAccount(profile: UserProfile, connection: MongoClientConnection) {
        //TODO: insert profile into db (suspend function)
        //val success = connection.insertUserProfile(profile)
//        if(success) {
//            println("Account created successfully")
//        }
//        else {
//            println("Account was unable to be added to the database!")
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
