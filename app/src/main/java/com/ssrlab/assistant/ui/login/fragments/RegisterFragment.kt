package com.ssrlab.assistant.ui.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.databinding.FragmentRegisterBinding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment

class RegisterFragment: BaseLaunchFragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var fireAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegisterBinding.inflate(layoutInflater)

        fireAuth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        setUpRegisterButton()
    }

    private fun setUpRegisterButton() {
        binding.apply {
            registerButton.setOnClickListener {
                if (registerEmailInput.text.toString().isNotEmpty() && registerPasswordInput.text.toString().isNotEmpty()) {
                    fireAuth.createUserWithEmailAndPassword(registerEmailInput.text.toString(), registerPasswordInput.text.toString())
                        .addOnCompleteListener {
                            registerDebugWindow.text = it.isSuccessful.toString()
                        }
                        .addOnFailureListener {
                            registerDebugWindow.text = it.message.toString()
                        }
                        .addOnSuccessListener {
                            registerDebugWindow.text = "${it.user?.uid}\n${fireAuth.currentUser?.email}"
                        }
                } else registerDebugWindow.text = "Email and password should not be empty"
            }
        }
    }
}