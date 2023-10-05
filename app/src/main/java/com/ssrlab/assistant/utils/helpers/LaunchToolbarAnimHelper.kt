package com.ssrlab.assistant.utils.helpers

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityChooseBinding
import com.ssrlab.assistant.ui.choose.ChooseActivity

class LaunchToolbarAnimHelper {

    fun setUpToolbar(chooseActivity: ChooseActivity, binding: ActivityChooseBinding, title: String = "", isBackButtonVisible: Boolean = false, isAdditionalButtonsVisible: Boolean = false, navController: NavController? = null) {
        binding.apply {
            if (title == "") chooseToolbarTitle.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_out))
            else chooseToolbarTitle.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_in))

            chooseToolbarTitle.animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) { chooseToolbarTitle.text = title }
                override fun onAnimationRepeat(p0: Animation?) {}
            })

            chooseToolbarBack.setOnClickListener { chooseActivity.onBackPressedDispatcher.onBackPressed() }

            if (navController != null) {
                chooseToolbarSettings.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_settingsFragment) }
                chooseToolbarContacts.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_contactsFragment) }
            }

            if (isBackButtonVisible) {
                chooseToolbarBack.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_in))
                chooseToolbarBack.visibility = View.VISIBLE
            } else {
                chooseToolbarBack.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_out))
                chooseToolbarBack.visibility = View.INVISIBLE
            }

            if (isAdditionalButtonsVisible) {
                chooseToolbarContacts.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_in))
                chooseToolbarSettings.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_in))
                chooseToolbarContacts.visibility = View.VISIBLE
                chooseToolbarSettings.visibility = View.VISIBLE
            } else {
                chooseToolbarContacts.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_out))
                chooseToolbarSettings.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_out))
                chooseToolbarContacts.visibility = View.GONE
                chooseToolbarSettings.visibility = View.INVISIBLE
            }
        }
    }
}