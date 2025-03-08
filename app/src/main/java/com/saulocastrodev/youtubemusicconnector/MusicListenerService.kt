package com.saulocastrodev.youtubemusicconnector

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import androidx.core.graphics.createBitmap


class MusicListenerService : NotificationListenerService() {
    private val TAG = "YouTubeMusicService"
    private val musicData = MutableLiveData<MusicInfo>()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn?.packageName == "com.google.android.apps.youtube.music") {
            val mediaSessionManager =
                getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
            val controllers = mediaSessionManager.getActiveSessions(
                ComponentName(
                    this,
                    MusicListenerService::class.java
                )
            )

            controllers.forEach { controller ->
                val metadata = controller.metadata
                if (metadata != null) {
                    val musicId = metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)
                    val playlistId = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
                    val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                    val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
                    val thumbnail = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)

                    sendBroadcast(Intent("com.saulocastrodev.MUSIC_UPDATED").apply {
                        putExtra("musicId", musicId)
                        putExtra("playlistId", "") // Se não disponível via metadata
                        putExtra("title", title)
                        putExtra("artist", artist)
                        putExtra("thumbnail", thumbnail)
                    })
                }
            }
        }
    }


    fun getMusicData(): LiveData<MusicInfo> {
        return musicData
    }

    fun extractMusicIdFromExtras(extras: Bundle): String {
        return extras.getString("music_id") ?: "unknown"
    }

    fun extractPlaylistIdFromExtras(extras: Bundle): String {
        return extras.getString("playlist_id") ?: "unknown"
    }

    fun sendNowPlayingToServer(musicInfo: MusicInfo, duration: Int) {
        val client = OkHttpClient()
        val url = "https://youtubeconnect.app.br/api/now-playing"

        val json = JSONObject().apply {
            put("music_id", musicInfo.musicId)
            put("playlist_id", musicInfo.playlistId)
            put("duration", duration)
        }


        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute()
    }
}

// Modelo de dados para armazenar as informações da música
data class MusicInfo(
    val musicId: String,
    val playlistId: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val thumbnail: Bitmap?
)