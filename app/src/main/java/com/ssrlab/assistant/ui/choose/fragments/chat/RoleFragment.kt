package com.ssrlab.assistant.ui.choose.fragments.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentRoleChooseBinding
import com.ssrlab.assistant.ui.choose.fragments.BaseChooseFragment
import com.ssrlab.assistant.utils.*

class RoleFragment: BaseChooseFragment() {

    private lateinit var binding: FragmentRoleChooseBinding

    private var chatId = ""
    private var chatTitle = ""
    private var chatImg = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRoleChooseBinding.inflate(layoutInflater)

        chatId = arguments?.getString(CHAT_ID) ?: INDEFINITE
        chatTitle = arguments?.getString(CHAT_TITLE) ?: INDEFINITE
        chatImg = arguments?.getInt(CHAT_IMAGE) ?: R.drawable.img_speaker_1

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        chooseActivity.setUpToolbar(resources.getString(R.string.role_title), isBackButtonVisible = true)
        setUpButtons()
    }

    private fun setUpButtons() {
        binding.apply {
            roleButton1Ripple.setOnClickListener(createClickListener(ROLE_1, 1, binding.roleButton1Title.text.toString()))
            roleButton2Ripple.setOnClickListener(createClickListener(ROLE_2, 2, binding.roleButton2Title.text.toString()))
            roleButton3Ripple.setOnClickListener(createClickListener(ROLE_3, 3, binding.roleButton3Title.text.toString()))
            roleButton4Ripple.setOnClickListener(createClickListener(ROLE_4, 4, binding.roleButton4Title.text.toString()))
            roleButton5Ripple.setOnClickListener(createClickListener(ROLE_5, 5, binding.roleButton5Title.text.toString()))
            roleButton6Ripple.setOnClickListener(createClickListener(ROLE_6, 6, binding.roleButton6Title.text.toString()))
            roleButton7Ripple.setOnClickListener(createClickListener(ROLE_7, 7, binding.roleButton7Title.text.toString()))
            roleButton8Ripple.setOnClickListener(createClickListener(ROLE_8, 8, binding.roleButton8Title.text.toString()))
            roleButton9Ripple.setOnClickListener(createClickListener(ROLE_9, 9, binding.roleButton9Title.text.toString()))
            roleButton10Ripple.setOnClickListener(createClickListener(ROLE_10, 10, binding.roleButton10Title.text.toString()))
            roleButton11Ripple.setOnClickListener(createClickListener(ROLE_11, 11, binding.roleButton11Title.text.toString()))
            roleButton12Ripple.setOnClickListener(createClickListener(ROLE_12, 12, binding.roleButton12Title.text.toString()))
            roleButton13Ripple.setOnClickListener(createClickListener(ROLE_13, 13, binding.roleButton13Title.text.toString()))
        }
    }

    private fun createClickListener(roleCode: String, roleInt: Int, role: String) : OnClickListener {
        return OnClickListener {
            chooseActivity.intentToChat(chatId, chatTitle, chatImg, roleCode, roleInt, role)
        }
    }
}