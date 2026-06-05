/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import com.google.android.gms.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.microg.gms.gcm.PushRegisterManager

class PushNotificationAppFragment : PreferenceFragmentCompat() {
    private lateinit var appHeadingPreference: AppHeadingPreference
    private lateinit var wakeForDelivery: TwoStatePreference
    private lateinit var allowRegister: TwoStatePreference
    private lateinit var status: Preference
    private lateinit var unregister: Preference
    private lateinit var unregisterCat: PreferenceCategory

    private val database get() = GcmDatabaseProvider.get(requireContext())
    private val packageName: String?
        get() = arguments?.getString("package")
    private var updateDetailsJob: Job? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_push_notifications_app)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(MaterialColors.getColor(view, android.R.attr.colorBackground))
    }

    @SuppressLint("RestrictedApi")
    override fun onBindPreferences() {
        appHeadingPreference = preferenceScreen.findPreference("pref_push_app_heading") ?: return
        wakeForDelivery = preferenceScreen.findPreference("pref_push_app_wake_for_delivery") ?: return
        allowRegister = preferenceScreen.findPreference("pref_push_app_allow_register") ?: return
        unregister = preferenceScreen.findPreference("pref_push_app_unregister") ?: return
        unregisterCat = preferenceScreen.findPreference("prefcat_push_app_unregister") ?: return
        status = preferenceScreen.findPreference("pref_push_app_status") ?: return
        wakeForDelivery.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val wake = newValue as Boolean
            lifecycleScope.launch {
                withContext(Dispatchers.IO) { database.setAppWakeForDelivery(packageName, wake) }
            }
            true
        }
        allowRegister.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as? Boolean ?: return@OnPreferenceChangeListener false
            lifecycleScope.launch {
                if (!enabled) {
                    val registrations = withContext(Dispatchers.IO) {
                        packageName?.let { database.getRegistrationsByApp(it) } ?: emptyList()
                    }
                    if (registrations.isNotEmpty()) {
                        showUnregisterConfirm(R.string.gcm_unregister_after_deny_message)
                    }
                }
                withContext(Dispatchers.IO) { database.setAppAllowRegister(packageName, enabled) }
            }
            true
        }
        unregister.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showUnregisterConfirm(R.string.gcm_unregister_confirm_message)
            true
        }
    }

    private fun showUnregisterConfirm(unregisterConfirmDesc: Int) {
        val ctx = context ?: return
        val pm = ctx.packageManager
        lifecycleScope.launch {
            val appLabel = withContext(Dispatchers.IO) {
                pm.getApplicationInfoIfExists(packageName)?.loadLabel(pm) ?: packageName
            }
            if (!isAdded) return@launch
            AlertDialog.Builder(ctx)
                .setIcon(R.drawable.ic_unregister)
                .setTitle(getString(R.string.gcm_unregister_confirm_title, appLabel))
                .setMessage(unregisterConfirmDesc)
                .setPositiveButton(android.R.string.ok) { _, _ -> unregister() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }.show()
        }
    }

    private fun unregister() {
        val pkg = packageName ?: return
        val appContext = requireContext().applicationContext
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                for (registration in database.getRegistrationsByApp(pkg)) {
                    PushRegisterManager.unregister(appContext, registration.packageName, registration.signature, null, null)
                }
            }
            updateDetails()
        }
    }

    override fun onResume() {
        super.onResume()
        updateDetailsJob?.cancel()
        updateDetailsJob = lifecycleScope.launch { updateDetails() }
    }

    override fun onPause() {
        super.onPause()
        updateDetailsJob?.cancel()
    }

    private suspend fun updateDetails() {
        if (!::appHeadingPreference.isInitialized) return
        appHeadingPreference.packageName = packageName
        val (app, registrations) = withContext(Dispatchers.IO) {
            val app = packageName?.let { database.getApp(it) }
            val registrations = packageName?.let { database.getRegistrationsByApp(it) } ?: emptyList()
            app to registrations
        }
        if (!isActive || !isAdded) return
        wakeForDelivery.isChecked = app?.wakeForDelivery ?: true
        allowRegister.isChecked = app?.allowRegister ?: true
        unregisterCat.isVisible = registrations.isNotEmpty()

        val ctx = context ?: return
        val sb = StringBuilder()
        if ((app?.totalMessageCount ?: 0L) == 0L) {
            sb.append(getString(R.string.gcm_no_message_yet))
        } else {
            sb.append(getString(R.string.gcm_messages_counter, app?.totalMessageCount, app?.totalMessageBytes))
            if (app?.lastMessageTimestamp != 0L) {
                sb.append("\n").append(getString(R.string.gcm_last_message_at, DateUtils.getRelativeDateTimeString(ctx, app?.lastMessageTimestamp ?: 0L, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME)))
            }
        }
        for (registration in registrations) {
            sb.append("\n")
            if (registration.timestamp == 0L) {
                sb.append(getString(R.string.gcm_registered))
            } else {
                sb.append(getString(R.string.gcm_registered_since, DateUtils.getRelativeDateTimeString(ctx, registration.timestamp, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME)))
            }
        }
        status.title = sb.toString()
    }

}
