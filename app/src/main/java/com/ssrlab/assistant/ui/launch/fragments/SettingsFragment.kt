package com.ssrlab.assistant.ui.launch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentSettingsBinding
import com.ssrlab.assistant.ui.launch.fragments.base.BaseFragment

class SettingsFragment: BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSettingsBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        launchActivity.setUpToolbar(resources.getString(R.string.settings_title), isBackButtonVisible = true)
    }
}