package com.ssrlab.assistant.ui.login.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssrlab.assistant.databinding.FragmentPassword3Binding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment

class ResetPassword3Fragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentPassword3Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPassword3Binding.inflate(layoutInflater)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        setShowPasswordButtons()
    }

    private fun setShowPasswordButtons() {
        inputHelper.showPasswordAction(binding.password3Input1, binding.password3Input1Show)
        inputHelper.showPasswordAction(binding.password3Input2, binding.password3Input2Show)
    }
}