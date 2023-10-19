package com.kod.assurancecontracthandler.views.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.databinding.SettingsActivityBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onDisplayPreferenceDialog(preference: Preference) {
            preference.setViewId(R.layout.root_preference_edit_text_dialog_preference)
            super.onDisplayPreferenceDialog(preference)

        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<ListPreference>(resources.getString(R.string.theme_mode_key))?.setOnPreferenceChangeListener { _, it2 ->
                val darkMode: String = it2 as String
                val themeValues = resources.getStringArray(R.array.entry_values_theme)
                when (darkMode) {
                    themeValues[1] -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }

                    themeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                true
            }

            findPreference<EditTextPreference>(resources.getString(R.string.predefined_message_key))?.setOnPreferenceChangeListener { _, newValue ->
                newValue as String
                return@setOnPreferenceChangeListener newValue.length <= 500
            }
        }
    }
}
