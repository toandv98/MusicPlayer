package com.toandv.demo.musicplayer.data

import com.toandv.demo.musicplayer.data.model.Song

interface SongDataSource {
    fun getAllSong(): List<Song>
}
