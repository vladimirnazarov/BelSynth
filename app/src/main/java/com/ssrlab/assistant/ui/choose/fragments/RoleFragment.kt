package com.ssrlab.assistant.ui.choose.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentRoleChooseBinding
import com.ssrlab.assistant.ui.choose.fragments.base.BaseFragment

class RoleFragment: BaseFragment() {

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

        chatId = arguments?.getString("chat_id") ?: "indefinite"
        chatTitle = arguments?.getString("chat_title") ?: "indefinite"
        chatImg = arguments?.getInt("chat_img") ?: R.drawable.img_speaker_1

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        chooseActivity.setUpToolbar(resources.getString(R.string.role_title), isBackButtonVisible = true)
        setUpButtons()
    }

    private fun setUpButtons() {
        binding.apply {
            roleButton1Ripple.setOnClickListener(createClickListener("assistant", 1))
            roleButton2Ripple.setOnClickListener(createClickListener("architect", 2))
            roleButton3Ripple.setOnClickListener(createClickListener("business_analyst", 3))
            roleButton4Ripple.setOnClickListener(createClickListener("financial_consultant", 4))
            roleButton5Ripple.setOnClickListener(createClickListener("recruiter", 5))
            roleButton6Ripple.setOnClickListener(createClickListener("project_manager", 6))
            roleButton7Ripple.setOnClickListener(createClickListener("legal_consultant", 7))
            roleButton8Ripple.setOnClickListener(createClickListener("marketing specialist", 8))
            roleButton9Ripple.setOnClickListener(createClickListener("realtor", 9))
            roleButton10Ripple.setOnClickListener(createClickListener("engineer", 10))
            roleButton11Ripple.setOnClickListener(createClickListener("programmer", 11))
            roleButton12Ripple.setOnClickListener(createClickListener("teacher", 12))
            roleButton13Ripple.setOnClickListener(createClickListener("writer", 13))
        }
    }

    private fun createClickListener(role: String, roleInt: Int) : OnClickListener {
        return OnClickListener {
            chooseActivity.intentToChat(chatId, chatTitle, chatImg, role, roleInt)
        }
    }
}