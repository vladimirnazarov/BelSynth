package com.ssrlab.assistant.ui.choose.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentSettingsBinding
import com.ssrlab.assistant.ui.choose.fragments.base.BaseChooseFragment

class SettingsFragment: BaseChooseFragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSettingsBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        chooseActivity.setUpToolbar(resources.getString(R.string.settings_title), isBackButtonVisible = true)
        setUpThemeSwitch()
        setUpRadioGroup()
        setUpAccountDelete()
        setUpSignOut()
    }

    private fun setUpThemeSwitch() {
        val isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.settingsThemeSwitch.isChecked = isChecked

        binding.settingsThemeSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            mainApp.saveTheme(checked, chooseActivity.sharedPreferences, chooseActivity)
        }
    }

    private fun setUpAccountDelete() {
        val textResources = arrayListOf(
            ContextCompat.getString(chooseActivity, R.string.delete_title),
            ContextCompat.getString(chooseActivity, R.string.delete_body),
            ContextCompat.getString(chooseActivity, R.string.delete_yes),
            ContextCompat.getString(chooseActivity, R.string.delete_no)
        )

        binding.settingsDelete.setOnClickListener {
            initDialog(textResources) {
                authClient.deleteUser({
                    mainApp.saveUserSignedIn(chooseActivity.sharedPreferences)
                    chooseActivity.intentBack()
                }, {
                    inputHelper.showErrorSnack(it, binding.root)
                })
            }
        }
    }

    private fun setUpSignOut() {
        val textResources = arrayListOf(
            ContextCompat.getString(chooseActivity, R.string.sign_out_title),
            ContextCompat.getString(chooseActivity, R.string.sign_out_body),
            ContextCompat.getString(chooseActivity, R.string.sign_out_yes),
            ContextCompat.getString(chooseActivity, R.string.sign_out_no)
        )

        binding.settingsLogOut.setOnClickListener {
            initDialog(textResources) {
                authClient.signOut {
                    mainApp.saveUserSignedIn(chooseActivity.sharedPreferences)
                    chooseActivity.intentBack()
                }
            }
        }
    }

    private fun setUpRadioGroup() {
        binding.settingsLanguageSwitch.apply {
            check(
                when (mainApp.getLocale()) {
                    "be" -> R.id.settings_language_be
                    "en" -> R.id.settings_language_en
                    "ru" -> R.id.settings_language_ru
                    "zh" -> R.id.settings_language_zh
                    else -> R.id.settings_language_be
                }
            )

            setOnCheckedChangeListener { _, i ->
                when (i) {
                    R.id.settings_language_be -> mainApp.savePreferences(chooseActivity.sharedPreferences, chooseActivity, "be")
                    R.id.settings_language_en -> mainApp.savePreferences(chooseActivity.sharedPreferences, chooseActivity, "en")
                    R.id.settings_language_ru -> mainApp.savePreferences(chooseActivity.sharedPreferences, chooseActivity, "ru")
                    R.id.settings_language_zh -> mainApp.savePreferences(chooseActivity.sharedPreferences, chooseActivity, "zh")
                    else -> mainApp.savePreferences(chooseActivity.sharedPreferences, chooseActivity, "en")
                }
            }
        }
    }
}