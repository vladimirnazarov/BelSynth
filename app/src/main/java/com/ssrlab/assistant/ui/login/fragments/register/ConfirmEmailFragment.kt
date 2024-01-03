package com.ssrlab.assistant.ui.login.fragments.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssrlab.assistant.databinding.FragmentEmailConfirmationBinding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment

class ConfirmEmailFragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentEmailConfirmationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentEmailConfirmationBinding.inflate(layoutInflater)

        return binding.root
    }
}