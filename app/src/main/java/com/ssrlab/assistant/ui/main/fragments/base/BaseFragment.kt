package com.ssrlab.assistant.ui.main.fragments.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.ui.main.ChooseActivity

open class BaseFragment: Fragment() {

    lateinit var chooseActivity: ChooseActivity
    lateinit var mainApp: MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chooseActivity = activity as ChooseActivity
        mainApp = chooseActivity.getMainApp()
    }
}