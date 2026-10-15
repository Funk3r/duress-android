package com.duress.app.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.duress.app.R
import com.duress.app.trigger.TriggerManager
import com.duress.app.trigger.TriggerSource

class DuressWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        try {
            if (intent.action == ACTION_TRIGGER_WIDGET) {
                TriggerManager.onTrigger(
                    context = context,
                    source = TriggerSource.WIDGET,
                    details = "Home Screen Widget tapped"
                )
            }
        } catch (_: Exception) {}
    }

    companion object {
        const val ACTION_TRIGGER_WIDGET = "com.duress.app.ACTION_TRIGGER_WIDGET"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            try {
                val views = RemoteViews(context.packageName, R.layout.duress_widget_layout)

                val triggerIntent = Intent(context, DuressWidgetProvider::class.java).apply {
                    action = ACTION_TRIGGER_WIDGET
                }

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    triggerIntent,
                    flags
                )

                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (_: Exception) {}
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val component = ComponentName(context, DuressWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id)
                }
            } catch (_: Exception) {}
        }
    }
}
