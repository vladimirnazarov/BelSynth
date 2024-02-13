package com.ssrlab.assistant.ui.login.fragments.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ssrlab.assistant.app.MainApplication
import com.ssrlab.assistant.client.AuthClient
import com.ssrlab.assistant.ui.login.LaunchActivity
import com.ssrlab.assistant.utils.helpers.TextHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

open class BaseLaunchFragment: Fragment() {

    lateinit var launchActivity: LaunchActivity
    lateinit var mainApp: MainApplication

    lateinit var authClient: AuthClient
    lateinit var inputHelper: TextHelper

    val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchActivity = activity as LaunchActivity
        mainApp = launchActivity.mainApp

        authClient = AuthClient(mainApp.getContext(), mainApp, launchActivity.sharedPreferences)
        inputHelper = TextHelper(mainApp.getContext())
    }
}