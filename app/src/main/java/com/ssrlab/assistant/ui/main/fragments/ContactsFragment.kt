package com.ssrlab.assistant.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentContactsBinding
import com.ssrlab.assistant.ui.main.fragments.base.BaseMainFragment

class ContactsFragment: BaseMainFragment() {

    private lateinit var binding: FragmentContactsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentContactsBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        chooseActivity.setUpToolbar(resources.getString(R.string.contacts_title), isBackButtonVisible = true)

        binding.apply {
            contactsEmail.setOnClickListener { chooseActivity.intentToMail() }
            contactsPhone1.setOnClickListener { chooseActivity.intentToPhone("+375173792126") }
            contactsPhone2.setOnClickListener { chooseActivity.intentToPhone("+375173792522") }
        }
    }
}