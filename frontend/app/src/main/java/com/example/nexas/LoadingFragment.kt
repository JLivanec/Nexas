package com.example.nexas

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.nexas.databinding.FragmentLoadingBinding

class LoadingFragment : Fragment() {

    // view binding
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        val view = binding.root

        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_loadingFragment_to_loginFragment)
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }, 1000)

        return view
    }

    // cleanup on destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}