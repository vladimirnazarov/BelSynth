package com.ssrlab.assistant.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentSettingsBinding
import com.ssrlab.assistant.ui.main.fragments.base.BaseMainFragment

class SettingsFragment: BaseMainFragment() {

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

        chooseActivity.setUpToolbar(resources.getString(R.string.settings_title), isBackButtonVisible = true)
        setUpThemeSwitch()
        setUpRadioGroup()
    }

    private fun setUpThemeSwitch() {
        val isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.settingsThemeSwitch.isChecked = isChecked

        binding.settingsThemeSwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            chooseActivity.saveTheme(checked)
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
                    else -> R.id.settings_language_en
                }
            )

            setOnCheckedChangeListener { _, i ->
                when (i) {
                    R.id.settings_language_be -> mainApp.savePreferences(chooseActivity.getSharedPreferences(), chooseActivity, "be")
                    R.id.settings_language_en -> mainApp.savePreferences(chooseActivity.getSharedPreferences(), chooseActivity, "en")
                    R.id.settings_language_ru -> mainApp.savePreferences(chooseActivity.getSharedPreferences(), chooseActivity, "ru")
                    R.id.settings_language_zh -> mainApp.savePreferences(chooseActivity.getSharedPreferences(), chooseActivity, "zh")
                    else -> mainApp.savePreferences(chooseActivity.getSharedPreferences(), chooseActivity, "en")
                }
            }
        }
    }
}