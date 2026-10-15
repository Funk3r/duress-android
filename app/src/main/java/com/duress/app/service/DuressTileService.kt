package com.duress.app.service

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.duress.app.R
import com.duress.app.trigger.TriggerManager
import com.duress.app.trigger.TriggerSource

class DuressTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        try {
            val tile = qsTile ?: return
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.qs_tile_label)
            tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_tile)
            tile.updateTile()
        } catch (_: Exception) {}
    }

    override fun onClick() {
        super.onClick()
        try {
            // Trigger emergency protocol
            TriggerManager.onTrigger(
                context = this,
                source = TriggerSource.QS_TILE,
                details = "Quick Settings Tile tapped"
            )

            // Flash active briefly for visual feedback if unlocked
            val tile = qsTile ?: return
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()

            // Reset after short delay
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
        } catch (_: Exception) {}
    }
}
