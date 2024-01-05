package com.ssrlab.assistant.ui.login.fragments.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentPasswordBinding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment

class ResetPasswordFragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPasswordBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        setUpButtons()
    }

    private fun setUpButtons() {
        val navController = findNavController()

        binding.passwordBack.setOnClickListener { navController.popBackStack() }

        setUpApplyButton()
    }

    private fun setUpApplyButton() {
        binding.passwordButton.setOnClickListener { checkLogin() }
    }

    private fun checkLogin() {
        binding.apply {
            if (passwordEmailInput.text?.isEmpty() == true) {
                val msg = ContextCompat.getString(launchActivity, R.string.empty_field_error)
                inputHelper.setEditTextError(passwordEmailInput, passwordEmailErrorTitle, passwordEmailErrorHolder, msg)
            }
        }
    }
}