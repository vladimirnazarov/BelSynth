package com.ssrlab.assistant.ui.launch.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ssrlab.assistant.databinding.FragmentChooseBinding
import com.ssrlab.assistant.ui.launch.fragments.base.BaseFragment

class ChooseFragment: BaseFragment() {

    private lateinit var binding: FragmentChooseBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentChooseBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        launchActivity.setUpToolbar("", isBackButtonVisible = false, isAdditionalButtonsVisible = true, findNavController())
        setUpChats()
    }

    private fun setUpChats() {
        binding.apply {
            chooseSpeaker1Ripple.setOnClickListener { launchActivity.intentToChat(1, binding.chooseSpeaker1Name.text.toString()) }
            chooseSpeaker2Ripple.setOnClickListener { launchActivity.intentToChat(2, binding.chooseSpeaker2Name.text.toString()) }
            chooseSpeaker3Ripple.setOnClickListener { launchActivity.intentToChat(3, binding.chooseSpeaker3Name.text.toString()) }
            chooseSpeaker4Ripple.setOnClickListener { launchActivity.intentToChat(4, binding.chooseSpeaker4Name.text.toString()) }
            chooseSpeaker5Ripple.setOnClickListener { launchActivity.intentToChat(5, binding.chooseSpeaker5Name.text.toString()) }
        }
    }
}