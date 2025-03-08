package com.saulocastrodev.youtubemusicconnector

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

class MainActivity : AppCompatActivity() {
    private lateinit var imgThumbnail: ImageView
    private lateinit var txtTitle: TextView
    private lateinit var txtArtist: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNotification: ImageButton
    private var isPlaying = false

    private val musicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                txtTitle.text = it.getStringExtra("title") ?: "Título indisponível"
                txtArtist.text = it.getStringExtra("artist") ?: "Artista indisponível"
                val thumbnail = it.getParcelableExtra<Bitmap>("thumbnail")
                if (thumbnail != null) {
                    imgThumbnail.setImageBitmap(thumbnail)
                } else {
                    imgThumbnail.setImageResource(R.drawable.ic_music_placeholder)
                }
            }
        }
    }

    fun isNotificationServiceEnabled(): Boolean {
        val cn = ComponentName(this, MusicListenerService::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgThumbnail = findViewById(R.id.imgThumbnail)
        txtTitle = findViewById(R.id.txtTitle)
        txtArtist = findViewById(R.id.txtArtist)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNotification = findViewById(R.id.btnNotification)

        val intentFilter = IntentFilter("com.saulocastrodev.youtubemusicconnector.ACTION_MUSIC_UPDATE")

        registerReceiver(
            musicReceiver,
            IntentFilter("com.saulocastrodev.MUSIC_UPDATED"),
            Context.RECEIVER_NOT_EXPORTED
        )


        btnPlayPause.setOnClickListener {
            isPlaying = !isPlaying
            btnPlayPause.setImageResource(if (isPlaying) R.drawable.pause_24px else R.drawable.play_arrow_24px)
        }

        btnNotification.setOnClickListener {
            if (!isNotificationServiceEnabled()) {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(musicReceiver)
    }


}