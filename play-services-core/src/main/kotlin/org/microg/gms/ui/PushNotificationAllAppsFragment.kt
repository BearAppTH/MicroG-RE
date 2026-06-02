/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PushNotificationAllAppsFragment : PreferenceFragmentCompat() {
    private val database get() = GcmDatabaseProvider.get(requireContext())
    private lateinit var registered: PreferenceCategory
    private lateinit var unregistered: PreferenceCategory
    private lateinit var registeredNone: Preference
    private lateinit var unregisteredNone: Preference
    private lateinit var progress: Preference

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
                updateContent()
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_push_notifications_all_apps)
        registered = preferenceScreen.findPreference("prefcat_push_apps_registered") ?: registered
        unregistered =
            preferenceScreen.findPreference("prefcat_push_apps_unregistered") ?: unregistered
        registeredNone =
            preferenceScreen.findPreference("pref_push_apps_registered_none") ?: registeredNone
        unregisteredNone =
            preferenceScreen.findPreference("pref_push_apps_unregistered_none") ?: unregisteredNone
        progress = preferenceScreen.findPreference("pref_push_apps_all_progress") ?: progress
    }

    private suspend fun updateContent() {
        val context = requireContext()
        val rawData = withContext(Dispatchers.IO) {
            val appList = database.appList
            val registrationsByPackage = database.registrationList.groupBy { it.packageName }
            appList.map { app -> app to (registrationsByPackage[app.packageName] ?: emptyList()) }
        }

        val apps = rawData.map { (app, registrations) ->
            val pref = AppIconPreference(context)
            pref.packageName = app.packageName
            pref.summary = if (app.lastMessageTimestamp > 0) {
                getString(
                    R.string.gcm_last_message_at,
                    DateUtils.getRelativeTimeSpanString(app.lastMessageTimestamp)
                )
            } else null
            pref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                findNavController().navigate(
                    requireContext(),
                    R.id.openGcmAppDetailsFromAll,
                    Bundle().apply { putString("package", app.packageName) }
                )
                true
            }
            pref.key = "pref_push_app_" + app.packageName
            pref to registrations
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
}
