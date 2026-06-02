/*
 * SPDX-FileCopyrightText: 2026 BearAppTH
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.microg.gms.gcm.GcmDatabase

class PushLogFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_push_log, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(MaterialColors.getColor(view, android.R.attr.colorBackground))

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_push_log)
        val emptyView = view.findViewById<TextView>(R.id.tv_push_log_empty)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val appContext = requireContext().applicationContext

        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                GcmDatabaseProvider.get(appContext).appList
                    .filter { it.lastMessageTimestamp > 0 }
                    .sortedByDescending { it.lastMessageTimestamp }
            }

            if (items.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = PushLogAdapter(items)
            }
        }
    }

    private inner class PushLogAdapter(private val items: List<GcmDatabase.App>) :
        RecyclerView.Adapter<PushLogAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.iv_app_icon)
            val appName: TextView = view.findViewById(R.id.tv_app_name)
            val lastMessage: TextView = view.findViewById(R.id.tv_last_message)
            val count: TextView = view.findViewById(R.id.tv_message_count)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_push_log, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            val pm = requireContext().packageManager

            try {
                val info = pm.getApplicationInfo(app.packageName, 0)
                holder.icon.setImageDrawable(pm.getApplicationIcon(info))
                holder.appName.text = pm.getApplicationLabel(info).toString()
            } catch (e: Exception) {
                holder.icon.setImageResource(R.drawable.ic_cloud_messaging)
                holder.appName.text = app.packageName
            }

            holder.lastMessage.text = getString(
                R.string.push_log_last_message,
                DateUtils.getRelativeTimeSpanString(
                    app.lastMessageTimestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
            )

            val count = app.totalMessageCount.toInt()
            holder.count.text = resources.getQuantityString(R.plurals.push_log_message_count, count, count)
        }

        override fun getItemCount() = items.size
    }
}
