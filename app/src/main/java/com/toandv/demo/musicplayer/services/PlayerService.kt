package com.toandv.demo.musicplayer.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media.app.NotificationCompat.MediaStyle
import com.toandv.demo.musicplayer.R
import com.toandv.demo.musicplayer.data.SongRepository
import com.toandv.demo.musicplayer.data.local.SongLocalDataSource
import com.toandv.demo.musicplayer.data.model.Song
import com.toandv.demo.musicplayer.ui.main.MainActivity
import com.toandv.demo.musicplayer.utils.Actions
import com.toandv.demo.musicplayer.utils.Extras
import com.toandv.demo.musicplayer.utils.ImageUtils
import com.toandv.demo.musicplayer.utils.Noti

class PlayerService : Service() {

    private val binder: LocalBinder = LocalBinder()
    private var musicPlayer: MusicPlayer? = null
    private var notificationManager: NotificationManagerCompat? = null
    private var builder: NotificationCompat.Builder? = null

    override fun onCreate() {
        super.onCreate()
        musicPlayer = MusicPlayer(this, listener)
        notificationManager = NotificationManagerCompat.from(this)

        createNotificationChannel()
        createNotificationBuilder()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.run {
            when (action) {
                Actions.ACTION_PLAY_OR_PAUSE -> playOrPause()
                Actions.ACTION_NEXT -> next()
                Actions.ACTION_PREVIOUS -> previous()
                Actions.ACTION_START_FOREGROUND -> {
                    startForeground(Noti.PLAYER_SERVICE_ID, builder?.build())
                    if (musicPlayer?.isPlaying == false) removeService()
                    else return@run
                }
                Actions.ACTION_CLOSE -> {
                    if (musicPlayer?.isPlaying == true) {
                        playOrPause()
                        removeService()
                    } else {
                        removeService()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    fun setSong(id: Long) = musicPlayer?.setSong(id)

    fun playOrPause() = musicPlayer?.playOrPause()

    fun loop() = musicPlayer?.loop()

    fun next() = musicPlayer?.next()

    fun previous() = musicPlayer?.previous()

    fun seekTo(pos: Int) = musicPlayer?.seekTo(pos)

    fun updatePlayList() {
        musicPlayer?.updatePlayList(
            SongRepository.getInstance(SongLocalDataSource.getInstance(this)).getAllSong()
        )
    }

    fun restoreDetail() {
        musicPlayer?.restoreDetail()
        stopForeground(false)
    }

    private val listener = object : PlayerListener {
        override fun onSongPrepared(song: Song, duration: Int) {
            val builder = this@PlayerService.builder ?: return
            Intent(Actions.ACTION_PREPARED).apply {
                song.duration = duration.toLong()
                putExtra(Extras.EXTRA_SONG, song)
                senBroadcast(this)
            }

            with(song) {
                builder.setContentText(artist)
                    .setContentTitle(title)
                    .setLargeIcon(
                        ImageUtils.bitmapFromUri(
                            this@PlayerService,
                            Uri.parse(thumbnail)
                        )
                    )
            }
            notificationManager?.notify(Noti.PLAYER_SERVICE_ID, builder.build())
        }

        override fun onPlayingChanged(isPlaying: Boolean) {
            Intent(Actions.ACTION_PLAYING_CHANGED).apply {
                putExtra(Extras.EXTRA_PLAYING, isPlaying)
                senBroadcast(this)
            }

            builder?.run {
                @SuppressLint("RestrictedApi")
                mActions[1] = if (isPlaying) actionPause else actionPlay

                setOngoing(isPlaying)
                notificationManager?.notify(Noti.PLAYER_SERVICE_ID, build())
            }
        }

        override fun onSeekChanged(position: Int) {
            Intent(Actions.ACTION_SEEK_CHANGED).apply {
                putExtra(Extras.EXTRA_POSITION, position)
                senBroadcast(this)
            }
        }

        override fun onError(msg: String) {
            Intent(Actions.ACTION_ERROR).apply {
                putExtra(Extras.EXTRA_MSG, msg)
                senBroadcast(this)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun senBroadcast(intent: Intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun removeService() {
        stopForeground(true)
        notificationManager?.cancel(Noti.PLAYER_SERVICE_ID)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Noti.PLAYER_CHANNEL_ID,
                getString(R.string.player_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager?.createNotificationChannel(
                notificationChannel
            )
        }
    }

    private val contentIntent
        get() = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }, 0)

    private val pendingPlay
        get() = PendingIntent.getService(this, 0, Intent(this, PlayerService::class.java).apply {
            action = Actions.ACTION_PLAY_OR_PAUSE
        }, 0)

    private val actionNext
        get() = NotificationCompat.Action(
            R.drawable.ic_baseline_skip_next_24, getString(R.string.action_button_next),
            PendingIntent.getService(this, 0, Intent(this, PlayerService::class.java).apply {
                action = Actions.ACTION_NEXT
            }, 0)
        )

    private val actionPrevious
        get() = NotificationCompat.Action(
            R.drawable.ic_baseline_skip_previous_24,
            getString(R.string.action_button_previous),
            PendingIntent.getService(this, 0, Intent(this, PlayerService::class.java).apply {
                action = Actions.ACTION_PREVIOUS
            }, 0)
        )

    private val actionClose
        get() = NotificationCompat.Action(
            R.drawable.ic_baseline_close_24,
            getString(R.string.action_button_close),
            PendingIntent.getService(this, 0, Intent(this, PlayerService::class.java).apply {
                action = Actions.ACTION_CLOSE
            }, 0)
        )

    private val actionPlay
        get() = NotificationCompat.Action(
            R.drawable.ic_baseline_play_arrow_32,
            getString(R.string.action_button_play),
            pendingPlay
        )

    private val actionPause
        get() = NotificationCompat.Action(
            R.drawable.ic_baseline_pause_32,
            getString(R.string.action_button_pause),
            pendingPlay
        )

    private fun createNotificationBuilder() {

        builder = NotificationCompat.Builder(this, Noti.PLAYER_CHANNEL_ID)
            .setNotificationSilent()
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.title_music_player))
            .setContentText(getString(R.string.title_music_player))
            .setStyle(MediaStyle().setShowActionsInCompactView(0, 1, 2))
            .setContentIntent(contentIntent)
            .addAction(actionPrevious)
            .addAction(actionPlay)
            .addAction(actionNext)
            .addAction(actionClose)
    }

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }
}
