package com.toandv.demo.musicplayer.ui.main

import com.toandv.demo.musicplayer.data.model.Song

interface MainContract {
    interface View {
        fun updateRecyclerView(list: List<Song>)

        fun updateSongDetail(song: Song)
    }

    interface Presenter {
        fun load()
    }
}
