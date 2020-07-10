package com.toandv.demo.musicplayer.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.toandv.demo.musicplayer.R
import com.toandv.demo.musicplayer.data.model.Song
import kotlinx.android.synthetic.main.item_song.view.*

class SongAdapter : ListAdapter<Song, SongAdapter.ViewHolder>(
    ItemCallBack()
) {

    private var onItemClick: ((Song) -> Unit)? = null

    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false),
            onItemClick
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class ViewHolder(itemView: View, onItemClick: ((Song) -> Unit)?) :
        RecyclerView.ViewHolder(itemView) {

        private var song: Song? = null

        init {
            if (onItemClick != null) {
                itemView.setOnClickListener { song?.let { onItemClick(it) } }
            }
        }

        fun onBind(song: Song) {
            with(song) {
                this@ViewHolder.song = this
                with(itemView) {
                    tvSongTitle.text = title
                    tvSongArtist.text = artist
                    Glide.with(context)
                        .load(thumbnail)
                        .placeholder(R.drawable.ic_baseline_album)
                        .centerInside()
                        .into(imageThumbnail)
                }
            }
        }
    }

    class ItemCallBack : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}
