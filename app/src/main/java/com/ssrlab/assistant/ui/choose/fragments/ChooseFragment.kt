package com.ssrlab.assistant.ui.choose.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ssrlab.assistant.R
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

        chooseActivity.setUpToolbar("", isBackButtonVisible = false, isAdditionalButtonsVisible = true, findNavController())
        setUpChats()
    }

    private fun setUpChats() {
        binding.apply {
            chooseSpeaker1Ripple.setOnClickListener { chooseActivity.intentToChat("ales", binding.chooseSpeaker1Name.text.toString(), R.drawable.img_speaker_1) }
            chooseSpeaker2Ripple.setOnClickListener { chooseActivity.intentToChat("alesia", binding.chooseSpeaker2Name.text.toString(), R.drawable.img_speaker_2) }
            chooseSpeaker3Ripple.setOnClickListener { chooseActivity.intentToChat("alena", binding.chooseSpeaker3Name.text.toString(), R.drawable.img_speaker_3) }
            chooseSpeaker4Ripple.setOnClickListener { chooseActivity.intentToChat("boris", binding.chooseSpeaker4Name.text.toString(), R.drawable.img_speaker_4) }
            chooseSpeaker5Ripple.setOnClickListener { chooseActivity.intentToChat("kiryl", binding.chooseSpeaker5Name.text.toString(), R.drawable.img_speaker_5) }
            chooseSpeaker6Ripple.setOnClickListener { chooseActivity.intentToChat("vasil", binding.chooseSpeaker6Name.text.toString(), R.drawable.img_speaker_6) }
        }
    }
}