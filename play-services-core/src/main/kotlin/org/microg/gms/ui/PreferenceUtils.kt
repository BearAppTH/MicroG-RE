/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import com.google.android.gms.R

internal fun chooseLayoutForPosition(index: Int, total: Int): Int = when {
    total <= 1 -> R.layout.preference_material_secondary_single
    total == 2 -> if (index == 0) R.layout.preference_material_secondary_top else R.layout.preference_material_secondary_bottom
    else -> when (index) {
        0 -> R.layout.preference_material_secondary_top
        total - 1 -> R.layout.preference_material_secondary_bottom
        else -> R.layout.preference_material_secondary_middle
    }
}
