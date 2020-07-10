package com.toandv.demo.musicplayer.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(
    var id: Long,
    var title: String,
    var source: String,
    var thumbnail: String,
    var album: String,
    var artist: String,
    var duration: Long
) : Parcelable
