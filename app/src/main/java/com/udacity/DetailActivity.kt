package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val name = intent.getStringExtra(MainActivity.NAME_KEY)
        val status = intent.getStringExtra(MainActivity.STATUS_KEY)

        file_name_text.text = name
        status_text.text = status
        if (status == "Failed")
            status_text.setTextColor(Color.RED)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()

        close_button.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

}
