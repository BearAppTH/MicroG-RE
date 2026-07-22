/*
 * SPDX-FileCopyrightText: 2022 microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.gmscompliance

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.android.gms.common.Feature
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.common.internal.ConnectionInfo
import com.google.android.gms.common.internal.GetServiceRequest
import com.google.android.gms.common.internal.IGmsCallbacks
import com.google.android.gms.gmscompliance.GmsDeviceComplianceResponse
import com.google.android.gms.gmscompliance.IGmsDeviceComplianceService
import com.google.android.gms.gmscompliance.IGmsDeviceComplianceServiceCallback
import org.microg.gms.BaseService
import org.microg.gms.common.GmsService

const val TAG = "DeviceCompliance"

class GmsDeviceComplianceService : BaseService(TAG, GmsService.GMS_COMPLIANCE) {
    override fun handleServiceRequest(callback: IGmsCallbacks, request: GetServiceRequest, service: GmsService) {
        callback.onPostInitCompleteWithConnectionInfo(CommonStatusCodes.SUCCESS, GmsDeviceComplianceServiceImpl(lifecycle).asBinder(), ConnectionInfo().apply {
            features = arrayOf(
                Feature("gmscompliance_api", 1)
            )
        });
    }
}

class GmsDeviceComplianceServiceImpl(override val lifecycle: Lifecycle) : IGmsDeviceComplianceService.Stub(), LifecycleOwner {
    override fun getDeviceCompliance(callback: IGmsDeviceComplianceServiceCallback?) {
        // NOTE: This is a stub. It always reports compliant = true and does not perform any
        // real Google attestation. It exists so apps that merely call this API don't crash,
        // not to defeat SafetyNet/Play Integrity checks. Server-side verification against
        // Google will still reject this response for apps that check strictly (e.g. banking
        // apps). See README "ข้อจำกัดที่ทราบอยู่แล้ว" for details.
        Log.w(TAG, "getDeviceCompliance() called - returning stub response (compliant=true, NOT a real attestation)")
        lifecycleScope.launch {
            try {
                callback?.onResponse(Status.SUCCESS, GmsDeviceComplianceResponse().apply { compliant = true })
            } catch (e: Exception) {
                Log.w(TAG, e)
            }
        }
    }
}
