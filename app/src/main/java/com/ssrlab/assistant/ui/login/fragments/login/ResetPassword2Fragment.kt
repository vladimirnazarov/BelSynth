package com.ssrlab.assistant.ui.login.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssrlab.assistant.databinding.FragmentPassword1Binding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment

class ResetPassword2Fragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentPassword1Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPassword1Binding.inflate(layoutInflater)

        return binding.root
    }
}