/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.microg.gms.gcm.GcmDatabase

class PushNotificationAllAppsFragment : PreferenceFragmentCompat() {
    private val database get() = GcmDatabaseProvider.get(requireContext())
    private lateinit var registered: PreferenceCategory
    private lateinit var unregistered: PreferenceCategory
    private lateinit var registeredNone: Preference
    private lateinit var unregisteredNone: Preference
    private lateinit var progress: Preference

    private data class AppDisplayData(
        val app: GcmDatabase.App,
        val registrations: List<GcmDatabase.Registration>,
        val label: CharSequence,
        val icon: Drawable?,
        val version: String?
    )

    private data class AppListSnapshot(val packageName: String, val isRegistered: Boolean)
    private var lastSnapshot: List<AppListSnapshot> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(MaterialColors.getColor(view, android.R.attr.colorBackground))

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    updateContent()
                    delay(UPDATE_INTERVAL)
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_push_notifications_all_apps)
        registered = preferenceScreen.findPreference("prefcat_push_apps_registered") ?: return
        unregistered = preferenceScreen.findPreference("prefcat_push_apps_unregistered") ?: return
        registeredNone = preferenceScreen.findPreference("pref_push_apps_registered_none") ?: return
        unregisteredNone = preferenceScreen.findPreference("pref_push_apps_unregistered_none") ?: return
        progress = preferenceScreen.findPreference("pref_push_apps_all_progress") ?: return
        lastSnapshot = emptyList()
    }

    private suspend fun updateContent() {
        if (!::registered.isInitialized) return
        val context = requireContext()
        val pm = context.packageManager
        val rawData = withContext(Dispatchers.IO) {
            val appList = database.appList
            val registrationsByPackage = database.registrationList.groupBy { it.packageName }
            appList.map { app ->
                val appInfo = pm.getApplicationInfoIfExists(app.packageName)
                val label: CharSequence = appInfo?.loadLabel(pm) ?: app.packageName
                val icon: Drawable? = appInfo?.loadIcon(pm)
                val version: String? = appInfo?.let {
                    try { pm.getPackageInfo(it.packageName, 0)?.versionName }
                    catch (_: PackageManager.NameNotFoundException) { null }
                }
                AppDisplayData(app, registrationsByPackage[app.packageName] ?: emptyList(), label, icon, version)
            }
        }

        val snapshot = rawData
            .sortedBy { it.app.packageName }
            .map { AppListSnapshot(it.app.packageName, it.registrations.isNotEmpty()) }
        val structurallyChanged = snapshot != lastSnapshot
        lastSnapshot = snapshot

        if (!structurallyChanged) {
            for (data in rawData) {
                val key = "pref_push_app_" + data.app.packageName
                val newSummary = if (data.app.lastMessageTimestamp > 0) {
                    getString(R.string.gcm_last_message_at, DateUtils.getRelativeTimeSpanString(data.app.lastMessageTimestamp))
                } else null
                (registered.findPreference<Preference>(key) ?: unregistered.findPreference<Preference>(key))?.summary = newSummary
            }
            return
        }

        val apps = rawData.map { data ->
            val pref = AppIconPreference(context)
            pref.setApplicationData(data.app.packageName, data.label, data.icon, data.version)
            pref.summary = if (data.app.lastMessageTimestamp > 0) {
                getString(
                    R.string.gcm_last_message_at,
                    DateUtils.getRelativeTimeSpanString(data.app.lastMessageTimestamp)
                )
            } else null
            pref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                findNavController().navigate(
                    requireContext(),
                    R.id.openGcmAppDetailsFromAll,
                    Bundle().apply { putString("package", data.app.packageName) }
                )
                true
            }
            pref.key = "pref_push_app_" + data.app.packageName
            pref to data.registrations
        }.sortedBy {
            it.first.title?.toString()?.lowercase() ?: ""
        }.mapIndexed { idx, pair ->
            pair.first.order = idx
            pair
        }

        registered.removeAll()
        unregistered.removeAll()

        val registeredList = mutableListOf<Preference>()
        val unregisteredList = mutableListOf<Preference>()

        for ((pref, registrations) in apps) {
            if (registrations.isEmpty()) unregisteredList.add(pref)
            else registeredList.add(pref)
        }

        if (registeredList.isEmpty()) registeredList.add(registeredNone)
        if (unregisteredList.isEmpty()) unregisteredList.add(unregisteredNone)

        registeredList.forEachIndexed { index, pref ->
            pref.layoutResource = chooseLayoutForPosition(index, registeredList.size)
            registered.addPreference(pref)
        }

        unregisteredList.forEachIndexed { index, pref ->
            pref.layoutResource = chooseLayoutForPosition(index, unregisteredList.size)
            unregistered.addPreference(pref)
        }

        registered.isVisible = true
        unregistered.isVisible = true
        progress.isVisible = false
    }

    companion object {
        private const val UPDATE_INTERVAL = 5_000L
    }
}
