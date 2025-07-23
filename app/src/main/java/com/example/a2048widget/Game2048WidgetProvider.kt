package com.example.awidget2048

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class Game2048WidgetProvider : AppWidgetProvider() {

    companion object {
        private const val ACTION_MOVE_UP = "ACTION_MOVE_UP"
        private const val ACTION_MOVE_DOWN = "ACTION_MOVE_DOWN"
        private const val ACTION_MOVE_LEFT = "ACTION_MOVE_LEFT"
        private const val ACTION_MOVE_RIGHT = "ACTION_MOVE_RIGHT"
        private const val ACTION_RESET = "ACTION_RESET"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action
        val gameLogic = GameLogic(context)

        when (action) {
            ACTION_MOVE_UP -> gameLogic.move(Direction.UP)
            ACTION_MOVE_DOWN -> gameLogic.move(Direction.DOWN)
            ACTION_MOVE_LEFT -> gameLogic.move(Direction.LEFT)
            ACTION_MOVE_RIGHT -> gameLogic.move(Direction.RIGHT)
            ACTION_RESET -> gameLogic.resetGame()
        }

        // Update all widgets
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, Game2048WidgetProvider::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        for (widgetId in allWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_2048)
        val gameLogic = GameLogic(context)

        // Set up click listeners
        setupClickListeners(context, views)

        // Update grid display
        updateGridDisplay(context, views, gameLogic)

        // Update score
        views.setTextViewText(R.id.score_text, "Score: ${gameLogic.getScore()}")

        // Update game over status
        if (gameLogic.isGameOver()) {
            views.setTextViewText(R.id.status_text, "Game Over!")
            views.setTextColor(R.id.status_text, context.getColor(R.color.nothing_red))
        } else {
            views.setTextViewText(R.id.status_text, "2048")
            views.setTextColor(R.id.status_text, context.getColor(R.color.nothing_text))
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setupClickListeners(context: Context, views: RemoteViews) {
        // Movement buttons
        views.setOnClickPendingIntent(R.id.btn_up, createPendingIntent(context, ACTION_MOVE_UP))
        views.setOnClickPendingIntent(R.id.btn_down, createPendingIntent(context, ACTION_MOVE_DOWN))
        views.setOnClickPendingIntent(R.id.btn_left, createPendingIntent(context, ACTION_MOVE_LEFT))
        views.setOnClickPendingIntent(R.id.btn_right, createPendingIntent(context, ACTION_MOVE_RIGHT))

        // Reset button
        views.setOnClickPendingIntent(R.id.btn_reset, createPendingIntent(context, ACTION_RESET))
    }

    private fun createPendingIntent(context: Context, action: String): android.app.PendingIntent {
        val intent = Intent(context, Game2048WidgetProvider::class.java).apply {
            this.action = action
        }
        return android.app.PendingIntent.getBroadcast(
            context, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateGridDisplay(context: Context, views: RemoteViews, gameLogic: GameLogic) {
        val grid = gameLogic.getGrid()
        val tileIds = arrayOf(
            intArrayOf(R.id.tile_0_0, R.id.tile_0_1, R.id.tile_0_2, R.id.tile_0_3),
            intArrayOf(R.id.tile_1_0, R.id.tile_1_1, R.id.tile_1_2, R.id.tile_1_3),
            intArrayOf(R.id.tile_2_0, R.id.tile_2_1, R.id.tile_2_2, R.id.tile_2_3),
            intArrayOf(R.id.tile_3_0, R.id.tile_3_1, R.id.tile_3_2, R.id.tile_3_3)
        )

        for (i in 0..3) {
            for (j in 0..3) {
                val value = grid[i][j]
                val tileId = tileIds[i][j]

                if (value == 0) {
                    views.setTextViewText(tileId, "")
                    views.setInt(tileId, "setBackgroundColor", context.getColor(R.color.default_tile))
                } else {
                    views.setTextViewText(tileId, value.toString())
                    views.setInt(tileId, "setBackgroundColor", getTileColor(context, value))
                }

                views.setTextColor(tileId, context.getColor(R.color.tile_text_color))
            }
        }
    }

    private fun getTileColor(context: Context, value: Int): Int {
        return when (value) {
            0    -> context.getColor(R.color.tile_0)
            2    -> context.getColor(R.color.tile_2)
            4    -> context.getColor(R.color.tile_4)
            8    -> context.getColor(R.color.tile_8)
            16   -> context.getColor(R.color.tile_16)
            32   -> context.getColor(R.color.tile_32)
            64   -> context.getColor(R.color.tile_64)
            128  -> context.getColor(R.color.tile_128)
            256  -> context.getColor(R.color.tile_256)
            512  -> context.getColor(R.color.tile_512)
            1024 -> context.getColor(R.color.tile_1024)
            else -> context.getColor(R.color.tile_2048)
        }
    }
}
