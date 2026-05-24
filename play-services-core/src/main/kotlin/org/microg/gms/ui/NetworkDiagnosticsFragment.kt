/*
 * SPDX-FileCopyrightText: 2026 BearAppTH
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class NetworkDiagnosticsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_network_diagnostics, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(MaterialColors.getColor(view, android.R.attr.colorBackground))

        val btnRun = view.findViewById<Button>(R.id.btn_run_diagnostics)
        val tvCheckin = view.findViewById<TextView>(R.id.tv_checkin_result)
        val tvMcs = view.findViewById<TextView>(R.id.tv_mcs_result)
        val tvApis = view.findViewById<TextView>(R.id.tv_googleapis_result)

        btnRun.setOnClickListener {
            btnRun.isEnabled = false
            tvCheckin.text = getString(R.string.diagnostics_running)
            tvMcs.text = getString(R.string.diagnostics_running)
            tvApis.text = getString(R.string.diagnostics_running)

            lifecycleScope.launch {
                val checkinResult = testTcp("checkin.googleapis.com", 443)
                val mcsResult = testTcp("mtalk.google.com", 5228)
                val apisResult = testHttps("https://www.googleapis.com/")

                tvCheckin.applyResult(checkinResult)
                tvMcs.applyResult(mcsResult)
                tvApis.applyResult(apisResult)
                btnRun.isEnabled = true
            }
        }
    }

    private fun TextView.applyResult(result: DiagResult) {
        val ctx = context ?: return
        when (result) {
            is DiagResult.Ok -> {
                text = getString(R.string.diagnostics_result_ok, result.latencyMs)
                setTextColor(ContextCompat.getColor(ctx, R.color.md_theme_primary))
            }
            is DiagResult.Fail -> {
                text = getString(R.string.diagnostics_result_failed, result.reason)
                setTextColor(ContextCompat.getColor(ctx, R.color.md_theme_error))
            }
        }
    }

    private suspend fun testTcp(host: String, port: Int): DiagResult = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 5000)
            }
            DiagResult.Ok((System.currentTimeMillis() - start).toInt())
        } catch (e: Exception) {
            DiagResult.Fail(e.message ?: "Unknown error")
        }
    }

    private suspend fun testHttps(urlStr: String): DiagResult = withContext(Dispatchers.IO) {
        var conn: HttpsURLConnection? = null
        try {
            val start = System.currentTimeMillis()
            conn = URL(urlStr).openConnection() as HttpsURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.connect()
            val code = conn.responseCode
            if (code in 200..599) DiagResult.Ok((System.currentTimeMillis() - start).toInt())
            else DiagResult.Fail("HTTP $code")
        } catch (e: Exception) {
            DiagResult.Fail(e.message ?: "Unknown error")
        } finally {
            conn?.disconnect()
        }
    }

    private sealed class DiagResult {
        data class Ok(val latencyMs: Int) : DiagResult()
        data class Fail(val reason: String) : DiagResult()
    }
}
