package com.example.notificationpricereader

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private lateinit var db: PriceDatabase
    private var prices by mutableStateOf<List<PriceEntry>>(emptyList())
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val text = intent.getStringExtra("text") ?: return
            val packageName = intent.getStringExtra("package") ?: return

            findPrice(text)?.let { price ->
                db.addPrice(price, packageName, text)
                refreshPrices()
            }
        }
    }

    private var isReceiverRegistered = false // Flag to track receiver registration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = PriceDatabase(this)

        // Check if we have notification access
        if (!isNotificationServiceEnabled()) {
            showEnableNotificationAccessDialog()
        } else {
            registerReceiver(
                notificationReceiver,
                IntentFilter("com.example.notificationpricereader.NOTIFICATION")
            )
            isReceiverRegistered = true // Set flag to true when registered
        }

        refreshPrices()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Price History",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        items(prices) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "$${String.format("%.2f", entry.price)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.Green
                                    )
                                    Text(
                                        text = "From: ${entry.source}",
                                        style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Green
                                    )
                                    Text(
                                        text = dateFormat.format(Date(entry.timestamp)),
                                        style = MaterialTheme.typography.bodySmall,
                                       color = Color.Green
                                    )
                                    Text(
                                        text = entry.originalText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Green
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun refreshPrices() {
        prices = db.getAllPrices()
    }

    private fun findPrice(text: String): Double? {
        // Regex to match prices with either , or . as decimal separator, followed by kr or DKK
        val regex = """(\d+[\.,]\d{2})\s*(kr\.?|DKK)""".toRegex()
        val matchResult = regex.find(text)

        // Extract the matched number and convert comma to dot if needed
        return matchResult?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }

    private fun showEnableNotificationAccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Access Required")
            .setMessage("This app needs notification access to read prices. Please enable it in settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister only if registered
        if (isReceiverRegistered) {
            unregisterReceiver(notificationReceiver)
            isReceiverRegistered = false // Reset the flag
        }
    }
}
