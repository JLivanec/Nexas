package com.example.nexas
import com.example.nexas.data.MongoClientConnection
import com.example.nexas.model.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

        // Set up UI elements using binding
        binding.accCreateButton.setOnClickListener {
            //TODO: add parameter for account creation
            createAccount()
        }

        return binding.root
    }

    suspend private fun createAccount(profile: UserProfile) {

        val connection = MongoClientConnection()
        val success = connection.insertUserProfile(profile)
        if(success) {
            println("Account created successfully")
        }
        else {
            println("Account was unable to be added to the database!")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
