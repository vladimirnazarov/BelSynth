package com.ssrlab.assistant.utils.helpers.view

import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.NavController
import com.ssrlab.assistant.BaseActivity
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.ActivityChooseBinding

class LaunchToolbarAnimHelper(private val activity: BaseActivity) {

    private val alphaIn = AnimationUtils.loadAnimation(activity, R.anim.alpha_in)
    private val alphaOut = AnimationUtils.loadAnimation(activity, R.anim.alpha_out)

    fun setUpToolbar(binding: ActivityChooseBinding, title: String = "", isBackButtonVisible: Boolean = false, isAdditionalButtonsVisible: Boolean = false, navController: NavController? = null) {
        binding.apply {
            if (title == "") chooseActivityToolbarTitle.startAnimation(alphaOut)
            else chooseActivityToolbarTitle.startAnimation(alphaIn)

            chooseActivityToolbarTitle.text = title

            chooseActivityToolbarBack.setOnClickListener { activity.onBackPressedDispatcher.onBackPressed() }

            if (navController != null) {
                chooseActivityToolbarSettings.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_settingsFragment) }
                chooseActivityToolbarContacts.setOnClickListener { navController.navigate(R.id.action_chooseFragment_to_contactsFragment) }
            }

            if (isBackButtonVisible)
                controlAdditional(chooseActivityToolbarBack)
            else
                controlAdditional(chooseActivityToolbarBack, toShow = false)

            if (isAdditionalButtonsVisible)
                controlAdditional(chooseActivityToolbarContacts, chooseActivityToolbarSettings)
            else
                controlAdditional(chooseActivityToolbarContacts, chooseActivityToolbarSettings, false)
        }
    }

    private fun controlAdditional(view1: View, view2: View? = null, toShow: Boolean = true) {
        if (toShow) {
            view1.startAnimation(alphaIn)
            view1.visibility = View.VISIBLE

            if (view2 != null) {
                view2.startAnimation(alphaIn)
                view2.visibility = View.VISIBLE
            }
        } else {
            view1.startAnimation(alphaOut)
            view1.visibility = View.INVISIBLE

            if (view2 != null) {
                view2.startAnimation(alphaOut)
                view2.visibility = View.INVISIBLE
            }
        }
    }
}