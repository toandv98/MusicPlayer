package com.toandv.demo.musicplayer.services

import com.toandv.demo.musicplayer.data.model.Song

interface PlayerListener {
    fun onSongPrepared(song: Song, duration: Int)

    fun onPlayingChanged(isPlaying: Boolean)

    fun onSeekChanged(position: Int)

    fun onError(msg: String)
}
