/*
 * SPDX-FileCopyrightText: 2026 BearAppTH
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.android.gms.R
import org.microg.gms.checkin.CheckinPreferences
import org.microg.gms.gcm.GcmPrefs

class GmsStatusWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_gms_status)

            val checkinEnabled = CheckinPreferences.isEnabled(context)
            val gcmEnabled = GcmPrefs.get(context).isEnabled

            views.setTextViewText(
                R.id.widget_status,
                context.getString(if (checkinEnabled) R.string.widget_status_ok else R.string.widget_status_off)
            )
            views.setTextViewText(
                R.id.widget_gcm_status,
                context.getString(if (gcmEnabled) R.string.widget_gcm_on else R.string.widget_gcm_off)
            )

            val intent = Intent(context, MainSettingsActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
