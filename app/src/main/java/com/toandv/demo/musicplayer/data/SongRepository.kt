package com.toandv.demo.musicplayer.data

import com.toandv.demo.musicplayer.data.model.Song

class SongRepository(private val localDataSource: SongDataSource) : SongDataSource {

    override fun getAllSong(): List<Song> {
        return localDataSource.getAllSong()
    }

    companion object {
        @Volatile
        private var INSTANCE: SongRepository? = null

        fun getInstance(localDataSource: SongDataSource) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SongRepository(localDataSource)
            }
    }
}
