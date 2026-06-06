/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import org.microg.gms.checkin.CheckinPreferences
import org.microg.gms.checkin.getCheckinServiceInfo
import org.microg.gms.profile.ProfileManager
import org.microg.gms.profile.ProfileManager.PROFILE_AUTO
import org.microg.gms.profile.ProfileManager.PROFILE_NATIVE
import org.microg.gms.profile.ProfileManager.PROFILE_REAL
import org.microg.gms.profile.ProfileManager.PROFILE_SYSTEM
import org.microg.gms.profile.ProfileManager.PROFILE_USER
import java.io.File
import java.io.FileOutputStream

class DeviceRegistrationFragment : PreferenceFragmentCompat() {
    private lateinit var switchBarPreference: SwitchBarPreference
    private lateinit var deviceProfile: ListPreference
    private lateinit var importProfile: Preference
    private lateinit var serial: Preference
    private lateinit var statusCategory: PreferenceCategory
    private lateinit var status: Preference
    private lateinit var androidId: Preference
    private lateinit var profileFileImport: ActivityResultLauncher<String>

    private data class ProfileStatus(
        val entryValues: List<CharSequence>,
        val entries: List<CharSequence>,
        val value: String,
        val summary: String,
        val serial: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        profileFileImport = registerForActivityResult(ActivityResultContracts.GetContent(), this::onFileSelected)
    }

    private fun onFileSelected(uri: Uri?) {
        if (uri == null) return
        var file: File? = null
        try {
            val context = requireContext()
            file = File.createTempFile("profile_", ".xml", context.cacheDir)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return
            inputStream.use { FileOutputStream(file).use { out -> it.copyTo(out) } }
            val success = ProfileManager.importUserProfile(context, file)
            if (success && ProfileManager.isAutoProfile(context, PROFILE_USER)) {
                ProfileManager.setProfile(context, PROFILE_USER)
            }
            viewLifecycleOwner.lifecycleScope.launch { updateStatus() }
        } catch (e: Exception) {
            Log.w(TAG, e)
        } finally {
            file?.delete()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_device_registration)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(MaterialColors.getColor(view, android.R.attr.colorBackground))

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    updateStatus()
                    delay(UPDATE_INTERVAL)
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onBindPreferences() {
        switchBarPreference = preferenceScreen.findPreference("pref_checkin_enabled") ?: return
        deviceProfile = preferenceScreen.findPreference("pref_device_profile") ?: return
        importProfile = preferenceScreen.findPreference("pref_device_profile_import") ?: return
        serial = preferenceScreen.findPreference("pref_device_serial") ?: return
        statusCategory = preferenceScreen.findPreference("prefcat_device_registration_status") ?: return
        status = preferenceScreen.findPreference("pref_device_registration_status") ?: return
        androidId = preferenceScreen.findPreference("pref_device_registration_android_id") ?: return

        deviceProfile.setOnPreferenceChangeListener { _, newValue ->
            ProfileManager.setProfile(requireContext(), newValue as String? ?: PROFILE_AUTO)
            viewLifecycleOwner.lifecycleScope.launch { updateStatus() }
            true
        }
        importProfile.setOnPreferenceClickListener {
            profileFileImport.launch("text/xml")
            true
        }
        switchBarPreference.setOnPreferenceChangeListener { _, newValue ->
            val newStatus = newValue as Boolean
            CheckinPreferences.setEnabled(requireContext(), newStatus)
            true
        }

        serial.setOnPreferenceClickListener {
            val text = serial.summary?.toString()?.takeIf { it.isNotEmpty() }
                ?: return@setOnPreferenceClickListener true
            copyToClipboard("Serial", text)
            true
        }
        androidId.setOnPreferenceClickListener {
            val text = androidId.summary?.toString()?.takeIf { it.isNotEmpty() }
                ?: return@setOnPreferenceClickListener true
            copyToClipboard("Android ID", text)
            true
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(requireContext(), R.string.pref_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::switchBarPreference.isInitialized) return
        switchBarPreference.isChecked = CheckinPreferences.isEnabled(requireContext())
    }

    private suspend fun updateStatus() {
        if (!::deviceProfile.isInitialized) return
        val appContext = requireContext().applicationContext
        val profileStatus = withContext(Dispatchers.IO) { buildProfileStatus(appContext) }
        val serviceInfo = try { getCheckinServiceInfo(appContext) } catch (_: Exception) { return }
        deviceProfile.entryValues = profileStatus.entryValues.toTypedArray()
        deviceProfile.entries = profileStatus.entries.toTypedArray()
        deviceProfile.value = profileStatus.value
        deviceProfile.summary = profileStatus.summary
        serial.summary = profileStatus.serial
        statusCategory.isVisible = serviceInfo.configuration.enabled
        if (serviceInfo.lastCheckin > 0) {
            status.summary = getString(
                R.string.checkin_last_registration,
                DateUtils.getRelativeTimeSpanString(serviceInfo.lastCheckin, System.currentTimeMillis(), 0)
            )
            androidId.isVisible = true
            androidId.summary = serviceInfo.androidId.toString(16)
        } else {
            status.summary = getString(R.string.checkin_not_registered)
            androidId.isVisible = false
        }
    }

    private fun buildProfileStatus(appContext: Context): ProfileStatus {
        val configured = ProfileManager.getConfiguredProfile(appContext)
        val autoProfile = ProfileManager.getAutoProfile(appContext)
        val autoProfileName = when (autoProfile) {
            PROFILE_NATIVE -> appContext.getString(R.string.profile_name_native)
            PROFILE_REAL -> appContext.getString(R.string.profile_name_real)
            else -> ProfileManager.getProfileName(appContext, autoProfile) ?: autoProfile
        }
        val profiles = mutableListOf(PROFILE_AUTO, PROFILE_NATIVE, PROFILE_REAL)
        val profileNames = mutableListOf(
            appContext.getString(R.string.profile_name_auto, autoProfileName),
            appContext.getString(R.string.profile_name_native),
            appContext.getString(R.string.profile_name_real)
        )
        if (ProfileManager.hasProfile(appContext, PROFILE_SYSTEM)) {
            profiles.add(PROFILE_SYSTEM)
            profileNames.add(appContext.getString(R.string.profile_name_system, ProfileManager.getProfileName(appContext, PROFILE_SYSTEM)))
        }
        if (ProfileManager.hasProfile(appContext, PROFILE_USER)) {
            profiles.add(PROFILE_USER)
            profileNames.add(appContext.getString(R.string.profile_name_user, ProfileManager.getProfileName(appContext, PROFILE_USER)))
        }
        for (profile in BUILT_IN_PROFILES) {
            ProfileManager.getProfileName(appContext, profile)?.let { name ->
                profiles.add(profile)
                profileNames.add(name)
            }
        }
        val summary = profiles.indexOf(configured).takeIf { it >= 0 }?.let { profileNames[it] } ?: "Unknown"
        return ProfileStatus(
            entryValues = profiles,
            entries = profileNames,
            value = configured,
            summary = summary,
            serial = ProfileManager.getSerial(appContext)
        )
    }

    companion object {
        private const val UPDATE_INTERVAL = 1000L
        private const val TAG = "DeviceRegistrationFragment"
        private val BUILT_IN_PROFILES: List<String> by lazy {
            R.xml::class.java.declaredFields
                .map { it.name }
                .filter { it.startsWith("profile_") }
                .map { it.substring(8) }
                .sorted()
        }
    }
}
