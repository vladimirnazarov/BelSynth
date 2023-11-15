package com.ssrlab.assistant.utils.helpers

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityChooseBinding
import com.ssrlab.assistant.ui.main.ChooseActivity

class LaunchToolbarAnimHelper {

    fun setUpToolbar(chooseActivity: ChooseActivity, binding: ActivityChooseBinding, title: String = "", isBackButtonVisible: Boolean = false, isAdditionalButtonsVisible: Boolean = false, navController: NavController? = null) {
        binding.apply {
            if (title == "") chooseActivityToolbarTitle.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_out))
            else chooseActivityToolbarTitle.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_in))

            chooseActivityToolbarTitle.animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) { chooseActivityToolbarTitle.text = title }
                override fun onAnimationRepeat(p0: Animation?) {}
            })

            chooseActivityToolbarBack.setOnClickListener { chooseActivity.onBackPressedDispatcher.onBackPressed() }

            if (navController != null) {
                chooseActivityToolbarSettings.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_settingsFragment) }
                chooseActivityToolbarContacts.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_contactsFragment) }
            }

            if (isBackButtonVisible) {
                chooseActivityToolbarBack.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_in))
                chooseActivityToolbarBack.visibility = View.VISIBLE
            } else {
                chooseActivityToolbarBack.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_out))
                chooseActivityToolbarBack.visibility = View.INVISIBLE
            }

            if (isAdditionalButtonsVisible) {
                chooseActivityToolbarContacts.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_in))
                chooseActivityToolbarSettings.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_in))
                chooseActivityToolbarContacts.visibility = View.VISIBLE
                chooseActivityToolbarSettings.visibility = View.VISIBLE
            } else {
                chooseActivityToolbarContacts.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_out))
                chooseActivityToolbarSettings.startAnimation(AnimationUtils.loadAnimation(chooseActivity, R.anim.alpha_out))
                chooseActivityToolbarContacts.visibility = View.GONE
                chooseActivityToolbarSettings.visibility = View.INVISIBLE
            }
        }
    }
}