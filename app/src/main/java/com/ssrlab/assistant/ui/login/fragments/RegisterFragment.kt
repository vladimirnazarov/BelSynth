package com.ssrlab.assistant.ui.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.ssrlab.assistant.databinding.FragmentRegisterBinding
import com.ssrlab.assistant.ui.login.fragments.base.BaseLaunchFragment
import com.ssrlab.assistant.client.AuthClient

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

//        binding.registerGoogleRipple.setOnClickListener {
//            AuthClient().signIn(Identity.getSignInClient(mainApp.getContext()), {
//
//            }, { msg, type ->
//                println(msg)
//                println(type)
//            })
//        }
    }

    private fun setUpRegisterButton() {
        binding.apply {
            registerButton.setOnClickListener {
//                if (registerEmailInput.text.toString().isNotEmpty() && registerPasswordInput.text.toString().isNotEmpty()) {
//                    fireAuth.createUserWithEmailAndPassword(registerEmailInput.text.toString(), registerPasswordInput.text.toString())
//                        .addOnFailureListener {
//
//                        }
//                        .addOnSuccessListener {
//
//                        }
//                } else {
//
//                }

                val login = registerEmailInput.text.toString()
                val password = registerPasswordInput.text.toString()

                inputHelper.checkSignEmptiness(registerEmailInput, registerPasswordInput) {
                    when(it) {
                        1 -> {
                            authClient.signUp(login, password, {

                            }) { msg, type ->
                                inputHelper.handleErrorTypes(msg, type, binding)
                            }
                        }
                        2 -> {
                            //TODO
                        }
                        3 -> {
                            //TODO
                        }
                        4 -> {
                            //TODO
                        }
                    }
                }
            }
        }
    }


}