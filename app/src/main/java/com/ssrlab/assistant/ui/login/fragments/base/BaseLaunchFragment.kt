package com.ssrlab.assistant.ui.login.fragments.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.client.AuthClient
import com.ssrlab.assistant.ui.login.LaunchActivity
import com.ssrlab.assistant.utils.helpers.TextInputHelper

open class BaseLaunchFragment: Fragment() {

    lateinit var launchActivity: LaunchActivity
    lateinit var mainApp: MainApplication

    lateinit var authClient: AuthClient
    lateinit var inputHelper: TextInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchActivity = activity as LaunchActivity
        mainApp = launchActivity.getMainApp()

        authClient = AuthClient(mainApp.getContext())
        inputHelper = TextInputHelper(mainApp.getContext())
    }
}