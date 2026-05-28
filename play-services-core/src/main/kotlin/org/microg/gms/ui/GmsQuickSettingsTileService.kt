/*
 * SPDX-FileCopyrightText: 2026 BearAppTH
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.annotation.TargetApi
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.google.android.gms.R
import org.microg.gms.gcm.GcmPrefs

@TargetApi(Build.VERSION_CODES.N)
class GmsQuickSettingsTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        val isEnabled = GcmPrefs.get(applicationContext).isEnabled
        GcmPrefs.setEnabled(applicationContext, !isEnabled)
        refreshTile()
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        val isEnabled = GcmPrefs.get(applicationContext).isEnabled
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.service_name_mcs)
        tile.updateTile()
    }
}
