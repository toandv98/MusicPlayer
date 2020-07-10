package com.toandv.demo.musicplayer.utils

object Actions {
    const val ACTION_PREPARED = "prepared"
    const val ACTION_PLAYING_CHANGED = "playing_changed"
    const val ACTION_SEEK_CHANGED = "seek_changed"
    const val ACTION_ERROR = "error"

    const val ACTION_PLAY_OR_PAUSE = "play_or_pause"
    const val ACTION_NEXT = "skip_next"
    const val ACTION_PREVIOUS = "skip_previous"
    const val ACTION_CLOSE = "skip_close"
    const val ACTION_START_FOREGROUND = "start_foreground"
}

object Extras {
    const val EXTRA_SONG = "song"
    const val EXTRA_DURATION = "duration"
    const val EXTRA_MSG = "message"
    const val EXTRA_PLAYING = "is_playing"
    const val EXTRA_POSITION = "seek_position"
}

object Noti {
    const val PLAYER_CHANNEL_ID = "channel_player"
    const val PLAYER_SERVICE_ID = 98
}

object CODE {
    const val PERMISSION_REQUEST = 123
}
