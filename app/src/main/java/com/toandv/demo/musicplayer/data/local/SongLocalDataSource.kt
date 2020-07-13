package com.toandv.demo.musicplayer.data.local

import android.content.Context
import com.toandv.demo.musicplayer.data.SongDataSource
import com.toandv.demo.musicplayer.data.model.Song

class SongLocalDataSource(private val context: Context) : SongDataSource {

    override fun getAllSong(): List<Song> {
        return SongProvider.getAllDeviceSongs(context)
    }

    companion object {
        @Volatile
        private var INSTANCE: SongLocalDataSource? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SongLocalDataSource(context)
            }
    }
}
