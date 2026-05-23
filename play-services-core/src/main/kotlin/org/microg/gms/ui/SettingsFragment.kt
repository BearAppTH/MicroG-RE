/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.google.android.gms.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.microg.gms.checkin.CheckinPreferences
import org.microg.gms.common.ForegroundServiceOemUtils
import org.microg.gms.gcm.GcmDatabase
import org.microg.gms.gcm.GcmPrefs
import org.microg.gms.ui.settings.SettingsProvider
import org.microg.gms.ui.settings.getAllSettingsProviders
import org.microg.tools.ui.ResourceSettingsFragment

class SettingsFragment : ResourceSettingsFragment() {

    companion object {
        private const val TAG = "SettingsFragment"

        const val PREF_ABOUT = "pref_about"
        const val PREF_GCM = "pref_gcm"
        const val PREF_PRIVACY = "pref_privacy"
        const val PREF_CHECKIN = "pref_checkin"
        const val PREF_ACCOUNTS = "pref_accounts"
        const val PREF_HIDE_LAUNCHER_ICON = "pref_hide_launcher_icon"
        const val PREF_SELF_CHECK = "pref_self_check"
        const val PREF_GITHUB = "pref_github"
        const val PREF_IGNORE_BATTERY_OPTIMIZATION = "pref_ignore_battery_optimization"
        const val PREF_DARK_MODE = "pref_dark_mode"
        const val PREF_EXPORT_SETTINGS = "pref_export_settings"
        const val PREF_IMPORT_SETTINGS = "pref_import_settings"
        const val PREF_NETWORK_DIAGNOSTICS = "pref_network_diagnostics"

        private const val ACTIVITY_LAUNCHER_CONTROL = "org.microg.gms.ui.SettingsActivityLauncher"
        private const val PREF_GITHUB_URL = "https://github.com/BearAppTH/MicroG-RE"
    }

    private val createdPreferences = mutableListOf<Preference>()

    private val requestIgnoreBatteryOptimizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            updateBatteryOptimizationPreference()
        }

    private val exportSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        try {
                            requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                                SettingsBackupManager.export(requireContext(), out)
                            }
                            true
                        } catch (e: Exception) {
                            Log.e(TAG, "Export failed", e)
                            false
                        }
                    }
                    val ctx = context ?: return@launch
                    Toast.makeText(ctx, if (ok) R.string.export_success else R.string.export_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val importSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        try {
                            requireContext().contentResolver.openInputStream(uri)?.use { ins ->
                                SettingsBackupManager.import(requireContext(), ins)
                            } ?: false
                        } catch (e: Exception) {
                            Log.e(TAG, "Import failed", e)
                            false
                        }
                    }
                    val ctx = context ?: return@launch
                    if (ok) {
                        val mode = PreferenceManager.getDefaultSharedPreferences(ctx).getString(PREF_DARK_MODE, "system") ?: "system"
                        applyDarkMode(mode)
                    }
                    Toast.makeText(ctx, if (ok) R.string.import_success else R.string.import_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }

    init {
        preferencesResource = R.xml.preferences_start
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setupStaticPreferenceClickListeners()
        updateLauncherIconSwitchState()
        updateBatteryOptimizationPreference()
        updateAboutSummary()
        loadStaticEntries()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<ExtendedFloatingActionButton>(R.id.preference_fab)?.visibility =
            View.GONE
        updateBatteryOptimizationPreference()
        updateLauncherIconSwitchState()
        updateGcmSummary()
        updateCheckinSummary()
        updateDynamicEntries()
    }

    private fun setupStaticPreferenceClickListeners() {
        findPreference<Preference>(PREF_ACCOUNTS)?.setOnPreferenceClickListener {
            activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
                ?.selectedItemId = R.id.accountManagerFragment
            true
        }
        findPreference<Preference>(PREF_CHECKIN)?.setOnPreferenceClickListener {
            findNavController().navigate(requireContext(), R.id.openCheckinSettings)
            true
        }
        findPreference<Preference>(PREF_GCM)?.setOnPreferenceClickListener {
            findNavController().navigate(requireContext(), R.id.openGcmSettings)
            true
        }
        findPreference<Preference>(PREF_PRIVACY)?.setOnPreferenceClickListener {
            findNavController().navigate(requireContext(), R.id.openPrivacy)
            true
        }
        findPreference<SwitchPreferenceCompat>(PREF_HIDE_LAUNCHER_ICON)?.setOnPreferenceChangeListener { _, newValue ->
            val shouldHide = newValue as Boolean
            toggleLauncherIconVisibility(hide = shouldHide)
            true
        }
        findPreference<Preference>(PREF_SELF_CHECK)?.setOnPreferenceClickListener {
            findNavController().navigate(requireContext(), R.id.openSelfCheck)
            true
        }
        findPreference<Preference>(PREF_GITHUB)?.setOnPreferenceClickListener {
            openGithub()
            true
        }
        findPreference<Preference>(PREF_ABOUT)?.setOnPreferenceClickListener {
            findNavController().navigate(requireContext(), R.id.openAbout)
            true
        }
        findPreference<ListPreference>(PREF_DARK_MODE)?.setOnPreferenceChangeListener { _, newValue ->
            applyDarkMode(newValue as String)
            true
        }
        findPreference<Preference>(PREF_EXPORT_SETTINGS)?.setOnPreferenceClickListener {
            exportSettingsLauncher.launch("bear_microg_settings.json")
            true
        }
        findPreference<Preference>(PREF_IMPORT_SETTINGS)?.setOnPreferenceClickListener {
            importSettingsLauncher.launch("application/json")
            true
        }
        findPreference<Preference>(PREF_NETWORK_DIAGNOSTICS)?.setOnPreferenceClickListener {
            findNavController().navigate(requireContext(), R.id.openNetworkDiagnostics)
            true
        }
    }

    private fun applyDarkMode(mode: String) {
        val nightMode = when (mode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun updateAboutSummary() {
        findPreference<Preference>(PREF_ABOUT)?.summary = getString(
            R.string.about_version_str, AboutFragment.getAppVersion(context)
        )
    }

    private fun loadStaticEntries() {
        val ctx = context ?: return
        getAllSettingsProviders(ctx).flatMap { it.getEntriesStatic(ctx) }
            .forEach { entry -> entry.createPreference(ctx) }
    }

    private fun updateDynamicEntries() {
        lifecycleScope.launch {
            val ctx = context ?: return@launch
            val entries = getAllSettingsProviders(ctx).flatMap { it.getEntriesDynamic(ctx) }

            createdPreferences.forEach { preference ->
                if (entries.none { it.key == preference.key }) preference.isVisible = false
            }

            entries.forEach { entry ->
                val preference = createdPreferences.find { it.key == entry.key }
                if (preference != null) preference.fillFromEntry(entry)
                else entry.createPreference(ctx)
            }
        }
    }

    private val Context.isIgnoringBatteryOptimizations: Boolean
        get() = (getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isIgnoringBatteryOptimizations(
            packageName
        ) == true

    private fun updateBatteryOptimizationPreference() {
        val ctx = context ?: return
        findPreference<Preference>(PREF_IGNORE_BATTERY_OPTIMIZATION)?.apply {
            isVisible = !ctx.isIgnoringBatteryOptimizations
            setOnPreferenceClickListener {
                requestIgnoringBatteryOptimizations()
                true
            }
        }
    }

    private fun requestIgnoringBatteryOptimizations() {
        val ctx = context ?: return
        ForegroundServiceOemUtils.openBatteryOptimizationSettings(ctx) { intent ->
            requestIgnoreBatteryOptimizationLauncher.launch(intent)
        }
    }

    private fun toggleLauncherIconVisibility(hide: Boolean) {
        val newState = if (hide) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        val ctx = context ?: return
        val component = ComponentName(ctx, ACTIVITY_LAUNCHER_CONTROL)

        ctx.packageManager.setComponentEnabledSetting(
            component, newState, PackageManager.DONT_KILL_APP
        )
    }

    private fun updateLauncherIconSwitchState() {
        val ctx = context ?: return
        val component = ComponentName(ctx, ACTIVITY_LAUNCHER_CONTROL)
        val state = ctx.packageManager.getComponentEnabledSetting(component)

        val isHidden = state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        findPreference<SwitchPreferenceCompat>(PREF_HIDE_LAUNCHER_ICON)?.isChecked = isHidden
    }

    private fun updateGcmSummary() {
        val context = requireContext()
        val pref = findPreference<Preference>(PREF_GCM) ?: return

        if (GcmPrefs.get(context).isEnabled) {
            val database = GcmDatabase(context)
            val regCount = database.registrationList.size
            database.close()
            pref.summary =
                context.getString(org.microg.gms.base.core.R.string.service_status_enabled_short) + " - " + context.resources.getQuantityString(
                    R.plurals.gcm_registered_apps_counter, regCount, regCount
                )
        } else {
            pref.setSummary(org.microg.gms.base.core.R.string.service_status_disabled_short)
        }
    }

    private fun updateCheckinSummary() {
        val summaryRes =
            if (CheckinPreferences.isEnabled(requireContext())) org.microg.gms.base.core.R.string.service_status_enabled_short
            else org.microg.gms.base.core.R.string.service_status_disabled_short
        findPreference<Preference>(PREF_CHECKIN)?.setSummary(summaryRes)
    }

    private fun openGithub() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, PREF_GITHUB_URL.toUri()))
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Error opening link: $PREF_GITHUB_URL", e)
        }
    }

    private fun SettingsProvider.Companion.Entry.createPreference(context: Context): Preference? {
        val preference = Preference(context).fillFromEntry(this)
        val categoryKey = when (group) {
            SettingsProvider.Companion.Group.HEADER -> "prefcat_header"
            SettingsProvider.Companion.Group.GOOGLE -> "prefcat_google_services"
            SettingsProvider.Companion.Group.OTHER -> "prefcat_other_services"
            SettingsProvider.Companion.Group.FOOTER -> "prefcat_footer"
        }
        return try {
            findPreference<PreferenceCategory>(categoryKey)?.addPreference(preference)?.let {
                if (it) createdPreferences.add(preference)
                preference
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed adding preference $key", e)
            null
        }
    }

    private fun Preference.fillFromEntry(entry: SettingsProvider.Companion.Entry): Preference {
        key = entry.key
        title = entry.title
        summary = entry.summary
        icon = entry.icon
        isPersistent = false
        isVisible = true
        setOnPreferenceClickListener {
            findNavController().navigate(context, entry.navigationId)
            true
        }
        return this
    }
}