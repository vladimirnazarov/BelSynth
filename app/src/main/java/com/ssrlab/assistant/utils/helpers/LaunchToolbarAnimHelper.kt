package com.ssrlab.assistant.utils.helpers

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityLaunchBinding
import com.ssrlab.assistant.ui.launch.LaunchActivity

class LaunchToolbarAnimHelper {

    fun setUpToolbar(launchActivity: LaunchActivity, binding: ActivityLaunchBinding, title: String = "", isBackButtonVisible: Boolean = false, isAdditionalButtonsVisible: Boolean = false, navController: NavController? = null) {
        binding.apply {
            if (title == "") launchToolbarTitle.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.alpha_out))
            else launchToolbarTitle.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.alpha_in))

            launchToolbarTitle.animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) { launchToolbarTitle.text = title }
                override fun onAnimationRepeat(p0: Animation?) {}
            })

            launchToolbarBack.setOnClickListener { launchActivity.onBackPressedDispatcher.onBackPressed() }

            if (navController != null) {
                launchToolbarSettings.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_settingsFragment) }
                launchToolbarContacts.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_contactsFragment) }
            }

            if (isBackButtonVisible) {
                launchToolbarBack.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.alpha_in))
                launchToolbarBack.visibility = View.VISIBLE
            } else {
                launchToolbarBack.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.alpha_out))
                launchToolbarBack.visibility = View.INVISIBLE
            }

            if (isAdditionalButtonsVisible) {
                launchToolbarContacts.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.alpha_in))
                launchToolbarSettings.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.alpha_in))
                launchToolbarContacts.visibility = View.VISIBLE
                launchToolbarSettings.visibility = View.VISIBLE
            } else {
                launchToolbarContacts.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.alpha_out))
                launchToolbarSettings.startAnimation(AnimationUtils.loadAnimation(launchActivity, R.anim.alpha_out))
                launchToolbarContacts.visibility = View.GONE
                launchToolbarSettings.visibility = View.INVISIBLE
            }
        }
    }
}