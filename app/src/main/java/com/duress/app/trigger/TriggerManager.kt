package com.duress.app.trigger

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.duress.app.R
import com.duress.app.pipeline.EmergencyPipelineExecutor

object TriggerManager {

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Entry point when ANY trigger activates (Key Sequence, QS Tile, Widget, Test Button).
     * Rule: Every action is failure-isolated and toggleable.
     * Rule: Log which trigger path fired (key sequence, tile, widget), on-device only.
     */
    fun onTrigger(context: Context, source: TriggerSource, details: String = "") {
        EmergencyPipelineExecutor.execute(context, source, details)

        if (source == TriggerSource.TEST_BUTTON) {
            mainHandler.post {
                try {
                    val message = context.getString(R.string.trigger_fired_toast, source.displayNameEn)
                    Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {}
            }
        }
    }
}
