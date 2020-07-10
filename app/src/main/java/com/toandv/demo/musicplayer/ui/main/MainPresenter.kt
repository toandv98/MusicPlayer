package com.toandv.demo.musicplayer.ui.main

import com.toandv.demo.musicplayer.data.SongDataSource

class MainPresenter(private val view: MainContract.View, private val songRepo: SongDataSource) :
    MainContract.Presenter {

    override fun load() {
        songRepo.getAllSong().run {
            if (isNotEmpty()) {
                view.updateRecyclerView(this)
                view.updateSongDetail(first())
            }
        }
    }
}
