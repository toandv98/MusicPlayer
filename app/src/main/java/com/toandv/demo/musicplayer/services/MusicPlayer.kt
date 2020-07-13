package com.toandv.demo.musicplayer.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import com.toandv.demo.musicplayer.data.model.Song
import java.util.*

class MusicPlayer(private val context: Context, private val listener: PlayerListener) {

    private val player: MediaPlayer = MediaPlayer()
    private val playList = mutableListOf<Song>()
    private var timer: Timer? = null
    private var isPrepared = false
    private var pos = -1

    init {
        with(player) {

            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )

            setOnPreparedListener {
                isPrepared = true
                listener.onSongPrepared(playList[pos], player.duration)
                it.start()
                listener.onPlayingChanged(true)
                startUpdate()
            }

            setOnCompletionListener {
                isPrepared = false
                listener.onPlayingChanged(false)
                if (!it.isLooping) next()
                stopUpdate()
            }

            setOnErrorListener { _, what, extra ->
                listener.onError("Error($what, $extra)")
                stopUpdate()
                false
            }
        }
    }

    fun updatePlayList(list: List<Song>) {
        playList.clear()
        playList.addAll(list)
        if (pos == -1 && playList.isNotEmpty()) pos = 0
    }

    fun setSong(id: Long): Boolean {
        pos = playList.indexOfFirst {
            it.id == id
        }
        return when (pos) {
            -1 -> false
            else -> {
                playCurrentSong()
                true
            }
        }
    }

    fun playOrPause() {
        when {
            player.isPlaying -> {
                player.pause()
                listener.onPlayingChanged(false)
                stopUpdate()
            }
            isReady && player.isPlaying.not() -> when {
                isPrepared -> {
                    player.start()
                    listener.onPlayingChanged(true)
                    startUpdate()
                }
                else -> playCurrentSong()
            }
        }

    }

    fun loop() {
        player.isLooping = !player.isLooping
    }

    fun next() {
        if (isReady) {
            pos = (pos + 1) % playList.size
            playCurrentSong()
        }
    }

    fun previous() {
        if (isReady) {
            pos = (pos + playList.lastIndex) % playList.size
            playCurrentSong()
        }
    }

    fun seekTo(newPos: Int) {
        if (isReady && isPrepared) player.seekTo(newPos)
    }

    fun restoreDetail() {
        if (isReady && isPrepared) {
            listener.onSongPrepared(playList[pos], player.duration)
            listener.onPlayingChanged(isPlaying)
            if (isPlaying) listener.onSeekChanged(player.currentPosition)
        }
    }

    val isPlaying get() = player.isPlaying

    private val isReady get() = playList.isNotEmpty() && pos != -1

    private fun startUpdate() {
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    if (player.isPlaying) listener.onSeekChanged(player.currentPosition)
                }
            }, 0, 1000)
        }
    }

    private fun stopUpdate() {
        timer?.run {
            cancel()
            purge()
        }
    }

    private fun playCurrentSong() {
        with(player) {
            reset()
            setDataSource(context, Uri.parse(playList[pos].source))
            prepareAsync()
        }
    }
}
