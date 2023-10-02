package com.ssrlab.assistant.ui.choose.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ssrlab.assistant.databinding.FragmentChooseBinding
import com.ssrlab.assistant.ui.choose.fragments.base.BaseFragment

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
            chooseSpeaker1Ripple.setOnClickListener { launchActivity.intentToChat("ales", binding.chooseSpeaker1Name.text.toString()) }
            chooseSpeaker2Ripple.setOnClickListener { launchActivity.intentToChat("alesia", binding.chooseSpeaker2Name.text.toString()) }
            chooseSpeaker3Ripple.setOnClickListener { launchActivity.intentToChat("alena", binding.chooseSpeaker3Name.text.toString()) }
            chooseSpeaker4Ripple.setOnClickListener { launchActivity.intentToChat("boris", binding.chooseSpeaker4Name.text.toString()) }
            chooseSpeaker5Ripple.setOnClickListener { launchActivity.intentToChat("kiryl", binding.chooseSpeaker5Name.text.toString()) }
            chooseSpeaker6Ripple.setOnClickListener { launchActivity.intentToChat("vasil", binding.chooseSpeaker6Name.text.toString()) }
        }
    }
}