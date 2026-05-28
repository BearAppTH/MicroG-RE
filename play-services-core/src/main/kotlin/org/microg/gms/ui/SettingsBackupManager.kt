/*
 * SPDX-FileCopyrightText: 2026 BearAppTH
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import org.json.JSONObject
import org.microg.gms.checkin.CheckinPreferences
import org.microg.gms.gcm.GcmPrefs
import java.io.InputStream
import java.io.OutputStream

object SettingsBackupManager {
    private const val TAG = "SettingsBackup"
    private const val EXPORT_VERSION = 1

    fun export(context: Context, outputStream: OutputStream) {
        val json = JSONObject()
        json.put("export_version", EXPORT_VERSION)
        json.put("export_timestamp", System.currentTimeMillis())

        val gcm = GcmPrefs.get(context)
        json.put("gcm_enabled", gcm.isEnabled)
        json.put("gcm_confirm_new_apps", gcm.confirmNewApps)
        json.put("gcm_network_mobile", gcm.networkMobile)
        json.put("gcm_network_wifi", gcm.networkWifi)
        json.put("gcm_network_roaming", gcm.networkRoaming)
        json.put("gcm_network_other", gcm.networkOther)

        json.put("checkin_enabled", CheckinPreferences.isEnabled(context))

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        json.put("pref_dark_mode", prefs.getString("pref_dark_mode", "system"))
        json.put("pref_hide_launcher_icon", prefs.getBoolean("pref_hide_launcher_icon", true))

        outputStream.write(json.toString(2).toByteArray(Charsets.UTF_8))
        outputStream.flush()
        Log.d(TAG, "Settings exported")
    }

    fun import(context: Context, inputStream: InputStream): Boolean {
        return try {
            val text = inputStream.bufferedReader(Charsets.UTF_8).readText()
            val json = JSONObject(text)

            if (json.optInt("export_version", 0) != EXPORT_VERSION) {
                Log.w(TAG, "Unknown export version — proceeding anyway")
            }

            if (json.has("gcm_enabled")) GcmPrefs.setEnabled(context, json.getBoolean("gcm_enabled"))
            if (json.has("checkin_enabled")) CheckinPreferences.setEnabled(context, json.getBoolean("checkin_enabled"))

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            if (json.has("pref_dark_mode")) editor.putString("pref_dark_mode", json.getString("pref_dark_mode"))
            if (json.has("pref_hide_launcher_icon")) editor.putBoolean("pref_hide_launcher_icon", json.getBoolean("pref_hide_launcher_icon"))
            editor.apply()

            Log.d(TAG, "Settings imported")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            false
        }
    }
}
