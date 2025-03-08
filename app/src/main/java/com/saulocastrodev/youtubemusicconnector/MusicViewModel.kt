package com.saulocastrodev.youtubemusicconnector

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicViewModel : ViewModel() {
    private val _musicLiveData = MutableLiveData<MusicInfo>()
    val musicLiveData: LiveData<MusicInfo> get() = _musicLiveData

    fun updateMusicInfo(musicId: String, playlistId: String, title: String?, artist: String?, album: String?, thumbnail: Bitmap?) {
        _musicLiveData.postValue(MusicInfo(musicId, playlistId, title, artist, album, thumbnail))
    }
}