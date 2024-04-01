package com.ssrlab.assistant.ui.login.fragments.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentEmailConfirmationBinding
import com.ssrlab.assistant.ui.choose.ChooseActivity
import com.ssrlab.assistant.ui.login.fragments.BaseLaunchFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    override fun onStart() {
        super.onStart()

        startDelayedAnimation()
        setUpBottomTextAction()
        checkIfEmailVerified()
    }

    private fun checkIfEmailVerified() {
        authClient.awaitIfUserIsVerified(scope) {
            activity?.runOnUiThread {
                launchActivity.intentNext(launchActivity, ChooseActivity())
            }
        }
    }

    private fun setUpBottomTextAction() {
        binding.emailConfAction.setOnClickListener {
            authClient.sendVerificationEmail({}, {
                createErrorSnack(it)
            })
        }
    }

    private fun startDelayedAnimation() {
        val alphaInAnim = AnimationUtils.loadAnimation(launchActivity, R.anim.medium_alpha_in)
        scope.launch {
            delay(5000)
            launchActivity.runOnUiThread {
                binding.emailConfBottomHolder.apply {
                    startAnimation(alphaInAnim)
                    visibility = View.VISIBLE
                }
            }
        }
    }

    private fun createErrorSnack(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).apply {
            setTextColor(ContextCompat.getColor(context, R.color.snack_text))
            setBackgroundTint(ContextCompat.getColor(context, R.color.error))
            show()
        }
    }
}