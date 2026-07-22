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

package org.microg.gms.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

public class DeviceIdentifier {
    private static final String PREF_FILE = "microg_device_identifier";
    private static final String PREF_WIFI_MAC = "wifi_mac";
    private static final String PREF_MEID = "meid";

    public String wifiMac = randomMacAddress();
    public String meid = randomMeid();
    public String esn;

    /**
     * Loads a {@link DeviceIdentifier} whose {@link #wifiMac} and {@link #meid} are generated
     * once per install and then persisted, instead of being re-randomized on every call.
     * <p>
     * Checkin identity (see {@code CheckinManager}/{@code CheckinClient}) is expected to stay
     * stable across requests; regenerating these values on every checkin made the device look
     * like a "new" device to the checkin backend each time, which can affect checkin/GCM
     * registration reliability. This does not change behavior for {@link #esn}, which callers
     * still need to set explicitly if used.
     */
    public static DeviceIdentifier getPersisted(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        DeviceIdentifier identifier = new DeviceIdentifier();

        String wifiMac = prefs.getString(PREF_WIFI_MAC, null);
        String meid = prefs.getString(PREF_MEID, null);
        if (wifiMac == null || meid == null) {
            wifiMac = identifier.wifiMac;
            meid = identifier.meid;
            prefs.edit()
                    .putString(PREF_WIFI_MAC, wifiMac)
                    .putString(PREF_MEID, meid)
                    .apply();
        }

        identifier.wifiMac = wifiMac;
        identifier.meid = meid;
        return identifier;
    }

    private static String randomMacAddress() {
        String mac = "b407f9";
        Random rand = new Random();
        for (int i = 0; i < 6; i++) {
            mac += Integer.toString(rand.nextInt(16), 16);
        }
        return mac;
    }

    private static String randomMeid() {
        // http://en.wikipedia.org/wiki/International_Mobile_Equipment_Identity
        // We start with a known base, and generate random MEID
        String meid = "35503104";
        Random rand = new Random();
        for (int i = 0; i < 6; i++) {
            meid += Integer.toString(rand.nextInt(10));
        }

        // Luhn algorithm (check digit)
        int sum = 0;
        for (int i = 0; i < meid.length(); i++) {
            int c = Integer.parseInt(String.valueOf(meid.charAt(i)));
            if ((meid.length() - i - 1) % 2 == 0) {
                c *= 2;
                c = c % 10 + c / 10;
            }

            sum += c;
        }
        final int check = (100 - sum) % 10;
        meid += Integer.toString(check);

        return meid;
    }
}
