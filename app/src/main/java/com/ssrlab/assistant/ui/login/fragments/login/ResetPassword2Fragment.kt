package com.ssrlab.assistant.ui.login.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentPassword2Binding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment

class ResetPassword2Fragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentPassword2Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPassword2Binding.inflate(layoutInflater)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        //temp
        binding.password2Body.setOnClickListener {
            findNavController().navigate(R.id.action_resetPassword2Fragment_to_resetPassword3Fragment)
        }
    }
}