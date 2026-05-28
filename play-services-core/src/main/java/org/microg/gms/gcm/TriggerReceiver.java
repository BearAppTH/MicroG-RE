/*
 * Copyright (C) 2013-2017 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.microg.gms.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import org.microg.gms.checkin.CheckinPreferences;
import org.microg.gms.checkin.LastCheckinInfo;
import org.microg.gms.common.ForegroundServiceContext;

import static org.microg.gms.gcm.McsConstants.ACTION_CONNECT;
import static org.microg.gms.gcm.McsConstants.ACTION_HEARTBEAT;
import static org.microg.gms.gcm.McsConstants.EXTRA_REASON;

public class TriggerReceiver extends BroadcastReceiver {
    private static final String TAG = "GmsGcmTrigger";
    public static final String FORCE_TRY_RECONNECT = "org.microg.gms.gcm.FORCE_TRY_RECONNECT";
    private static boolean registered = false;

    /**
     * "Project Svelte" is just there to f**k things up...
     */
    public synchronized static void register(Context context) {
        if (!registered) {
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            ContextCompat.registerReceiver(context.getApplicationContext(), new TriggerReceiver(), intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
            registered = true;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            boolean force = "android.provider.Telephony.SECRET_CODE".equals(intent.getAction()) || FORCE_TRY_RECONNECT.equals(intent.getAction());
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (!GcmPrefs.get(context).isEnabled() && !force) {
                Log.d(TAG, "Ignoring " + intent + ": gcm is disabled");
                return;
            }

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                McsService.resetCurrentDelay();
            }

            if (LastCheckinInfo.read(context).getAndroidId() == 0) {
                Log.d(TAG, "Ignoring " + intent + ": need to checkin first.");
                if (CheckinPreferences.isEnabled(context)) {
                    Log.d(TAG, "Requesting check-in...");
                    String action = "android.server.checkin.CHECKIN";
                    Class<?> clazz = org.microg.gms.checkin.TriggerReceiver.class;
                    context.sendBroadcast(new Intent(action, null, context, clazz));
                }
                return;
            }

            force |= "android.intent.action.BOOT_COMPLETED".equals(intent.getAction());

            if (!force) {
                Network network = cm.getActiveNetwork();
                NetworkCapabilities caps = network != null ? cm.getNetworkCapabilities(network) : null;
                boolean isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                if (!isConnected) {
                    Log.d(TAG, "Ignoring " + intent + ": network is offline, scheduling new attempt.");
                    McsService.scheduleReconnect(context);
                    return;
                } else if (!GcmPrefs.get(context).isEnabledFor(caps)) {
                    Log.d(TAG, "Ignoring " + intent + ": gcm is disabled for active network");
                    return;
                }
            }

            ForegroundServiceContext fsc = new ForegroundServiceContext(context);
            if (!McsService.isConnected(context) || force) {
                Log.d(TAG, "Not connected to GCM but should be, asking the service to start up. Triggered by: " + intent);
                fsc.startService(new Intent(ACTION_CONNECT, null, context, McsService.class)
                        .putExtra(EXTRA_REASON, intent));
            } else {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    Log.d(TAG, "Ignoring " + intent + ": service is running. schedule reconnect instead.");
                    McsService.scheduleReconnect(context);
                } else {
                    Log.d(TAG, "Ignoring " + intent + ": service is running. heartbeat instead.");
                    fsc.startService(new Intent(ACTION_HEARTBEAT, null, context, McsService.class)
                            .putExtra(EXTRA_REASON, intent));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

}
