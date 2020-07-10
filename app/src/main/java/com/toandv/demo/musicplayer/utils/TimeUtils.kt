package com.toandv.demo.musicplayer.utils

object TimeUtils {

    fun toTimer(millis: Int): String {

        val hours = (millis / (1000 * 60 * 60))
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000)

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }
}
