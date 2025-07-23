package com.example.awidget2048

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
    }

    private fun setupViews() {
        val titleText = findViewById<TextView>(R.id.intro_title)
        val descriptionText = findViewById<TextView>(R.id.intro_description)
        val instructionsText = findViewById<TextView>(R.id.intro_instructions)
        val addWidgetButton = findViewById<Button>(R.id.btn_add_widget)
        val widgetStatusText = findViewById<TextView>(R.id.widget_status)

        // Set up click listener for add widget button
        addWidgetButton.setOnClickListener {
            openWidgetPicker()
        }

        // Check if widget is already added
        updateWidgetStatus(widgetStatusText)
    }

    private fun openWidgetPicker() {
        try {
            // Try to open widget picker directly
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to home screen settings
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        }
    }

    private fun updateWidgetStatus(statusText: TextView) {  // Change parameter type to TextView
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val thisWidget = ComponentName(this, Game2048WidgetProvider::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        if (allWidgetIds.isNotEmpty()) {
            statusText.text = "✓ Widget is active on your home screen!"
            statusText.setTextColor(getColor(R.color.nothing_text))
        } else {
            statusText.text = "Widget not added yet. Tap the button above to add it."
            statusText.setTextColor(getColor(R.color.nothing_text_secondary))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh widget status when returning to the activity
        val widgetStatusText = findViewById<TextView>(R.id.widget_status)
        updateWidgetStatus(widgetStatusText)
    }
}
