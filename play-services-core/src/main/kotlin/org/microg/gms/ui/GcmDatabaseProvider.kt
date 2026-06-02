/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.content.Context
import org.microg.gms.gcm.GcmDatabase

internal object GcmDatabaseProvider {
    @Volatile private var instance: GcmDatabase? = null

    fun get(context: Context): GcmDatabase =
        instance ?: synchronized(this) {
            instance ?: GcmDatabase(context.applicationContext).also { instance = it }
        }
}
