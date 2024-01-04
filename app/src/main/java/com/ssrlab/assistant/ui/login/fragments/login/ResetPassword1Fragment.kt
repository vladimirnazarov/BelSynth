package com.ssrlab.assistant.ui.login.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentPassword1Binding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment

class ResetPassword1Fragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentPassword1Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPassword1Binding.inflate(layoutInflater)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val navController = findNavController()

        binding.password1Back.setOnClickListener {
            navController.popBackStack()
        }

        //temp
        binding.password1Button.setOnClickListener {
            navController.navigate(R.id.action_resetPassword1Fragment_to_resetPassword2Fragment)
        }
    }
}