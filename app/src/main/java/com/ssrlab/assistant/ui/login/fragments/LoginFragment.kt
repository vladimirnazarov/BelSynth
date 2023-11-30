package com.ssrlab.assistant.ui.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.databinding.FragmentLoginBinding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment

class LoginFragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var fireAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(layoutInflater)

        fireAuth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        setUpLoginButton()
    }

    private fun setUpLoginButton() {
        binding.apply {
            loginButton.setOnClickListener {
                if (loginEmailInput.text.toString().isNotEmpty() && loginPasswordInput.text.toString().isNotEmpty()) {
                    fireAuth.signInWithEmailAndPassword(loginEmailInput.text.toString(), loginPasswordInput.text.toString())
                        .addOnFailureListener {

                        }
                        .addOnSuccessListener {

                        }
                } else {

                }
            }
        }
    }
}