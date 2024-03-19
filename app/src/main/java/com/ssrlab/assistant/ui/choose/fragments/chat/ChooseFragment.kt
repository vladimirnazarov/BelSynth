package com.ssrlab.assistant.ui.choose.fragments.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentChooseBinding
import com.ssrlab.assistant.ui.choose.fragments.BaseChooseFragment
import com.ssrlab.assistant.utils.BOT_1
import com.ssrlab.assistant.utils.BOT_2
import com.ssrlab.assistant.utils.BOT_3
import com.ssrlab.assistant.utils.BOT_4
import com.ssrlab.assistant.utils.BOT_5
import com.ssrlab.assistant.utils.BOT_6
import com.ssrlab.assistant.utils.CHAT_ID
import com.ssrlab.assistant.utils.CHAT_IMAGE
import com.ssrlab.assistant.utils.CHAT_TITLE

class ChooseFragment: BaseChooseFragment() {

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
            chooseDonateButton.setOnClickListener { chooseActivity.intentToLink("https://boosty.to/asistent.ai/donate") }
            choosePromoteRipple.setOnClickListener { chooseActivity.intentToLink("https://docs.google.com/forms/d/1Xey4v8z7X2xxppEWpjg6uYlKC3YILYBBrwFpumd8zXs/edit") }
            chooseSpeaker1Ripple.setOnClickListener { chooseActivity.intentToChat(BOT_1, binding.chooseSpeaker1Name.text.toString(), R.drawable.img_speaker_1) }
            chooseSpeaker2Ripple.setOnClickListener { chooseActivity.intentToChat(BOT_2, binding.chooseSpeaker2Name.text.toString(), R.drawable.img_speaker_2) }
            chooseSpeaker3Ripple.setOnClickListener { chooseActivity.intentToChat(BOT_3, binding.chooseSpeaker3Name.text.toString(), R.drawable.img_speaker_3) }
            chooseSpeaker4Ripple.setOnClickListener { chooseActivity.intentToChat(BOT_4, binding.chooseSpeaker4Name.text.toString(), R.drawable.img_speaker_4) }
            chooseSpeaker5Ripple.setOnClickListener { chooseActivity.intentToChat(BOT_5, binding.chooseSpeaker5Name.text.toString(), R.drawable.img_speaker_5) }
            chooseSpeaker6Ripple.setOnClickListener {
                findNavController().navigate(
                    R.id.action_chooseFragment_to_roleFragment,
                    bundleOf(
                        Pair(CHAT_ID, BOT_6),
                        Pair(CHAT_TITLE, binding.chooseSpeaker6Name.text.toString()),
                        Pair(CHAT_IMAGE, R.drawable.img_speaker_6)
                    )
                )
            }
        }
    }
}