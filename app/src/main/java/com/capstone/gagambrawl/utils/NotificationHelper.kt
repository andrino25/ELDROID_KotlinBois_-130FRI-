package com.capstone.gagambrawl.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.capstone.gagambrawl.R
import com.capstone.gagambrawl.view.Dashboard.DashboardPage
import com.capstone.gagambrawl.api.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {
    private val channelId = "spider_notifications"
    private val channelName = "Spider Management"
    private val notificationManager = NotificationManagerCompat.from(context)
    private val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://gagambrawl-api.vercel.app/")
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for spider management actions"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSpiderNotification(
        spiderName: String,
        action: SpiderAction,
        spiderId: String,
        token: String
    ) {
        val intent = Intent(context, DashboardPage::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("token", token)
            putExtra("open_inventory", true)
            putExtra("target_spider_name", spiderName)
            putExtra("notification_action", action.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            spiderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = when (action) {
            SpiderAction.ADD -> "Spider $spiderName added successfully"
            SpiderAction.UPDATE -> "Spider $spiderName updated successfully"
            SpiderAction.DELETE -> "Spider $spiderName deleted successfully"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_spider_logo)
            .setContentTitle("Spider Management")
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(spiderId.hashCode(), notification)
            }
        } else {
            notificationManager.notify(spiderId.hashCode(), notification)
        }
    }

    fun clear() {
        notificationScope.cancel()
    }

    enum class SpiderAction {
        ADD, UPDATE, DELETE
    }
}
