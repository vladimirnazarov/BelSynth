package com.ssrlab.assistant.ui.launch.fragments.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ssrlab.assistant.ui.launch.LaunchActivity

open class BaseFragment: Fragment() {

    lateinit var launchActivity: LaunchActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchActivity = activity as LaunchActivity
    }
}