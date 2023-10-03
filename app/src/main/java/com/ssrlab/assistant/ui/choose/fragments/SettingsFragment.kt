package com.ssrlab.assistant.ui.choose.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.ssrlab.assistant.R
import com.ssrlab.assistant.databinding.FragmentSettingsBinding
import com.ssrlab.assistant.ui.choose.fragments.base.BaseFragment

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
                    R.id.settings_language_be -> chooseActivity.savePreferences("be")
                    R.id.settings_language_en -> chooseActivity.savePreferences("en")
                    R.id.settings_language_ru -> chooseActivity.savePreferences("ru")
                    R.id.settings_language_zh -> chooseActivity.savePreferences("zh")
                    else -> chooseActivity.savePreferences("en")
                }
            }
        }
    }
}