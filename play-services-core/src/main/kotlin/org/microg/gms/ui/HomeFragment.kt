/*
 * SPDX-FileCopyrightText: 2026 BearAppTH
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.accounts.AccountManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import org.microg.gms.auth.AuthConstants
import org.microg.gms.checkin.CheckinPreferences
import org.microg.gms.checkin.getCheckinServiceInfo
import org.microg.gms.gcm.GcmPrefs
import org.microg.gms.gcm.getGcmServiceInfo
import org.microg.gms.profile.ProfileManager

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(MaterialColors.getColor(view, android.R.attr.colorBackground))

        requireActivity().findViewById<ExtendedFloatingActionButton>(R.id.preference_fab)?.visibility = View.GONE

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)

        view.findViewById<MaterialCardView>(R.id.card_account)?.setOnClickListener {
            bottomNav?.selectedItemId = R.id.accountManagerFragment
        }
        view.findViewById<MaterialCardView>(R.id.card_push)?.setOnClickListener {
            bottomNav?.selectedItemId = R.id.gcmFragment
        }
        view.findViewById<MaterialCardView>(R.id.card_device)?.setOnClickListener {
            findNavController().navigate(R.id.openCheckinFromHome)
        }
        view.findViewById<MaterialCardView>(R.id.card_self_check)?.setOnClickListener {
            findNavController().navigate(R.id.openSelfCheckFromHome)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    updateStatus()
                    delay(UPDATE_INTERVAL)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<ExtendedFloatingActionButton>(R.id.preference_fab)?.visibility = View.GONE
    }

    private suspend fun updateStatus() {
        val appContext = requireContext().applicationContext
        val v = getView() ?: return

        try {
            val accounts = withContext(Dispatchers.IO) {
                AccountManager.get(appContext).getAccountsByType(AuthConstants.DEFAULT_ACCOUNT_TYPE)
            }
            v.findViewById<TextView>(R.id.tv_account_email)?.text =
                if (accounts.isNotEmpty()) accounts[0].name
                else getString(R.string.home_account_none)

            val checkinEnabled = CheckinPreferences.isEnabled(appContext)
            val gcmEnabled = GcmPrefs.get(appContext).isEnabled

            val (checkinInfo, gcmInfo) = coroutineScope {
                val c = async { if (checkinEnabled) getCheckinServiceInfo(appContext) else null }
                val g = async { if (gcmEnabled) getGcmServiceInfo(appContext) else null }
                c.await() to g.await()
            }

            val isRegistered = checkinEnabled && (checkinInfo?.lastCheckin ?: 0L) > 0

            val colorOk = ContextCompat.getColor(appContext, R.color.md_theme_primary)
            val colorError = ContextCompat.getColor(appContext, R.color.md_theme_error)
            val colorDisabled = ContextCompat.getColor(appContext, R.color.md_theme_onSurfaceVariant)

            val statusColor = when {
                isRegistered -> colorOk
                !checkinEnabled -> colorDisabled
                else -> colorError
            }

            v.findViewById<View>(R.id.view_status_dot)?.background =
                GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(statusColor)
                }

            v.findViewById<TextView>(R.id.tv_status_label)?.apply {
                text = when {
                    isRegistered -> getString(R.string.home_status_ok)
                    !checkinEnabled -> getString(R.string.home_status_disabled)
                    else -> getString(R.string.home_status_not_registered)
                }
                setTextColor(statusColor)
            }

            v.findViewById<TextView>(R.id.tv_status_subtitle)?.text = when {
                isRegistered -> getString(
                    R.string.checkin_last_registration,
                    DateUtils.getRelativeTimeSpanString(
                        checkinInfo?.lastCheckin ?: 0L, System.currentTimeMillis(), 0
                    )
                )
                !checkinEnabled -> getString(R.string.home_status_disabled_hint)
                else -> getString(R.string.checkin_not_registered)
            }

            v.findViewById<TextView>(R.id.tv_push_status)?.text = when {
                !gcmEnabled -> getString(R.string.home_push_disabled)
                gcmInfo?.connected == true -> getString(R.string.home_push_connected)
                else -> getString(R.string.home_push_disconnected)
            }

            val (pushAppsCount, profileName, serial) = withContext(Dispatchers.IO) {
                val count = if (gcmEnabled) GcmDatabaseProvider.get(appContext).registrationList.size else 0
                val p = ProfileManager.getConfiguredProfile(appContext)
                val pName = ProfileManager.getProfileName(appContext, p) ?: p
                val s = ProfileManager.getSerial(appContext)
                Triple(count, pName, s)
            }

            v.findViewById<TextView>(R.id.tv_push_apps_count)?.text = if (pushAppsCount > 0)
                resources.getQuantityString(R.plurals.home_push_apps_count, pushAppsCount, pushAppsCount)
            else
                ""
            v.findViewById<TextView>(R.id.tv_push_apps_count)?.visibility =
                if (pushAppsCount > 0) View.VISIBLE else View.GONE

            v.findViewById<TextView>(R.id.tv_device_profile)?.text =
                getString(R.string.home_profile_label, profileName)

            v.findViewById<TextView>(R.id.tv_device_serial)?.text =
                getString(R.string.home_serial_label, serial)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Status update failed silently; UI retains previous state
        }
    }

    companion object {
        private const val UPDATE_INTERVAL = 5_000L
    }
}
