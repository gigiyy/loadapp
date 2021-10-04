package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var downloadManager: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        notificationManager = getSystemService(NotificationManager::class.java)
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager


        custom_button.setOnClickListener {
            download()
        }

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        createChannel(
            getString(R.string.loadapp_notification_channel_id),
            getString(R.string.loadapp_notification_channel_name)
        )
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download Completed"

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            custom_button.downloadComplete()
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != null) {
                val query = DownloadManager.Query().apply {
                    setFilterById(id)
                }
                val cursor = downloadManager.query(query)
                var status: Int = 0
                if (cursor.moveToFirst()) {
                    status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                }
                val statusString = when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> "Success"
                    else -> "Failed"
                }

                val name = when (radioGroup.checkedRadioButtonId) {
                    R.id.download_option_1 -> getString(R.string.glide_description)
                    R.id.download_option_2 -> getString(R.string.loadapp_description)
                    R.id.download_option_3 -> getString(R.string.retrofit_description)
                    else -> ""
                }

                notificationManager.sendNotification(
                    makeIntent(name, statusString),
                    getString(R.string.download_finished, name.split("-")[0]),
                    applicationContext
                )
            } else {
                Toast.makeText(context, getString(R.string.download_not_found), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun makeIntent(name: String, status: String): PendingIntent {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        contentIntent.putExtra(NAME_KEY, name)
        contentIntent.putExtra(STATUS_KEY, status)
        return PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun download() {
        if (radioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(applicationContext, "Please select the library", Toast.LENGTH_SHORT)
                .show()
            return
        }
        custom_button.downloadStarted()
        val url = when (radioGroup.checkedRadioButtonId) {
            R.id.download_option_1 -> URL_GLIDE
            R.id.download_option_2 -> URL_LOADAPP
            R.id.download_option_3 -> URL_RETROFIT
            else -> ""
        }
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.download_libraries))
                .setDescription(getString(R.string.app_description))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "master.zip")

        // enqueue puts the download request in the queue.
        downloadID = downloadManager.enqueue(request)
    }

    companion object {
        private const val URL_GLIDE =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val URL_RETROFIT =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val URL_LOADAPP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        const val NAME_KEY = "name"
        const val STATUS_KEY = "status"
    }

}

private const val NOTIFICATION_ID = 0

fun NotificationManager.sendNotification(
    contentPendingIntent: PendingIntent,
    messageBody: String,
    context: Context
) {
    val builder = NotificationCompat.Builder(
        context,
        context.getString(R.string.loadapp_notification_channel_id)
    ).setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent).setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .addAction(
            R.drawable.ic_assistant_black_24dp,
            context.getString(R.string.check_detail),
            contentPendingIntent
        )

    notify(NOTIFICATION_ID, builder.build())
}

