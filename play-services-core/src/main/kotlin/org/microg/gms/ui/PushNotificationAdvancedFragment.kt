/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import com.google.android.gms.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.microg.gms.checkin.LastCheckinInfo
import org.microg.gms.gcm.*

class PushNotificationAdvancedFragment : PreferenceFragmentCompat() {
    private lateinit var confirmNewApps: TwoStatePreference
    private lateinit var networkMobile: ListPreference
    private lateinit var networkWifi: ListPreference
    private lateinit var networkRoaming: ListPreference
    private lateinit var networkOther: ListPreference

    private val database get() = GcmDatabaseProvider.get(requireContext())
    private var removeRegistersDialog: AlertDialog? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_push_notification_settings)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(MaterialColors.getColor(view, android.R.attr.colorBackground))

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    updateContent()
                    delay(UPDATE_INTERVAL)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeRegistersDialog?.dismiss()
        removeRegistersDialog = null
    }

    @SuppressLint("RestrictedApi")
    override fun onBindPreferences() {
        confirmNewApps = preferenceScreen.findPreference(GcmPrefs.PREF_CONFIRM_NEW_APPS) ?: return
        networkMobile = preferenceScreen.findPreference(GcmPrefs.PREF_NETWORK_MOBILE) ?: return
        networkWifi = preferenceScreen.findPreference(GcmPrefs.PREF_NETWORK_WIFI) ?: return
        networkRoaming = preferenceScreen.findPreference(GcmPrefs.PREF_NETWORK_ROAMING) ?: return
        networkOther = preferenceScreen.findPreference(GcmPrefs.PREF_NETWORK_OTHER) ?: return

        confirmNewApps.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val enable = newValue as Boolean
                val appContext = requireContext().applicationContext
                val ctx = context ?: return@OnPreferenceChangeListener true
                viewLifecycleOwner.lifecycleScope.launch {
                    val cfg = getGcmServiceInfo(appContext)?.configuration
                    if (cfg == null) {
                        Toast.makeText(ctx, R.string.gcm_push_service_unavailable, Toast.LENGTH_SHORT).show()
                    } else {
                        setGcmServiceConfiguration(appContext, cfg.copy(confirmNewApps = enable))
                    }
                    updateContent()
                }
                true
            }

        bindNetworkPref(networkMobile) { appContext, value ->
            val cfg = getGcmServiceInfo(appContext)?.configuration ?: return@bindNetworkPref false
            setGcmServiceConfiguration(appContext, cfg.copy(mobile = value))
            true
        }
        bindNetworkPref(networkWifi) { appContext, value ->
            val cfg = getGcmServiceInfo(appContext)?.configuration ?: return@bindNetworkPref false
            setGcmServiceConfiguration(appContext, cfg.copy(wifi = value))
            true
        }
        bindNetworkPref(networkRoaming) { appContext, value ->
            val cfg = getGcmServiceInfo(appContext)?.configuration ?: return@bindNetworkPref false
            setGcmServiceConfiguration(appContext, cfg.copy(roaming = value))
            true
        }
        bindNetworkPref(networkOther) { appContext, value ->
            val cfg = getGcmServiceInfo(appContext)?.configuration ?: return@bindNetworkPref false
            setGcmServiceConfiguration(appContext, cfg.copy(other = value))
            true
        }

        findPreference<Preference>("pref_remove_all_registers")
            ?.setOnPreferenceClickListener {
                showRemoveRegistersDialog()
                true
            }
    }

    private fun bindNetworkPref(
        pref: ListPreference,
        configure: suspend (android.content.Context, Int) -> Boolean
    ) {
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val appContext = requireContext().applicationContext
            val ctx = context ?: return@OnPreferenceChangeListener true
            viewLifecycleOwner.lifecycleScope.launch {
                val succeeded = (newValue as? String)?.toIntOrNull()?.let { configure(appContext, it) } ?: true
                if (!succeeded) Toast.makeText(ctx, R.string.gcm_push_service_unavailable, Toast.LENGTH_SHORT).show()
                updateContent()
            }
            true
        }
    }

    private suspend fun updateContent() {
        if (!::networkOther.isInitialized) return
        val appContext = context?.applicationContext ?: return
        val serviceInfo = getGcmServiceInfo(appContext) ?: return

        confirmNewApps.isChecked = serviceInfo.configuration.confirmNewApps
        networkMobile.value = serviceInfo.configuration.mobile.toString()
        networkMobile.summary = getSummaryString(serviceInfo.configuration.mobile, serviceInfo.learntMobileInterval)
        networkWifi.value = serviceInfo.configuration.wifi.toString()
        networkWifi.summary = getSummaryString(serviceInfo.configuration.wifi, serviceInfo.learntWifiInterval)
        networkRoaming.value = serviceInfo.configuration.roaming.toString()
        // Roaming uses mobile cellular, so it shares the same learnt heartbeat interval as mobile
        networkRoaming.summary = getSummaryString(serviceInfo.configuration.roaming, serviceInfo.learntMobileInterval)
        networkOther.value = serviceInfo.configuration.other.toString()
        networkOther.summary = getSummaryString(serviceInfo.configuration.other, serviceInfo.learntOtherInterval)
    }

    private fun getSummaryString(value: Int, learnt: Int): String = when (value) {
        -1 -> getString(R.string.push_notifications_summary_off)
        0 -> getString(R.string.push_notifications_summary_automatic, getHeartbeatString(learnt))
        else -> getString(R.string.push_notifications_summary_manual, getHeartbeatString(value * 60000))
    }

    private fun getHeartbeatString(heartbeatMs: Int): String {
        return if (heartbeatMs < 120000) {
            getString(R.string.push_notifications_summary_values_seconds, (heartbeatMs / 1000).toString())
        } else getString(R.string.push_notifications_summary_values_minutes, (heartbeatMs / 60000).toString())
    }

    @SuppressLint("SetTextI18n")
    private fun showRemoveRegistersDialog() {
        removeRegistersDialog?.dismiss()
        val dialog = AlertDialog.Builder(requireContext()).setIcon(R.drawable.ic_unregister)
            .setTitle(R.string.gcm_remove_registers_dialog_title)
            .setMessage(R.string.gcm_remove_registers_dialog_message)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null).create()
        removeRegistersDialog = dialog

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false

            var secondsLeft = 10
            positiveButton.text = "${getString(android.R.string.ok)} ($secondsLeft)"
            positiveButton.alpha = 0.6f

            viewLifecycleOwner.lifecycleScope.launch {
                while (secondsLeft > 0) {
                    delay(1_000)
                    secondsLeft--
                    positiveButton.text = "${getString(android.R.string.ok)} ($secondsLeft)"
                }

                positiveButton.text = getString(android.R.string.ok)
                positiveButton.alpha = 1f
                positiveButton.isEnabled = true
                positiveButton.setOnClickListener {
                    val appContext = context?.applicationContext ?: return@setOnClickListener
                    val db = database
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            LastCheckinInfo.clear(appContext)
                            db.resetDatabase()
                        }
                        context?.let { ctx ->
                            Toast.makeText(ctx, R.string.gcm_remove_registers_toast_message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    companion object {
        private const val UPDATE_INTERVAL = 5_000L
    }
}
