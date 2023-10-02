package com.ssrlab.assistant.ui.choose.fragments.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.ui.choose.LaunchActivity

open class BaseFragment: Fragment() {

    lateinit var launchActivity: LaunchActivity
    lateinit var mainApp: MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchActivity = activity as LaunchActivity
        mainApp = launchActivity.getMainApp()
    }
}