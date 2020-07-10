package com.toandv.demo.musicplayer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.toandv.demo.musicplayer.R
import com.toandv.demo.musicplayer.data.Song
import kotlinx.android.synthetic.main.item_song.view.*

class SongAdapter : ListAdapter<Song, SongAdapter.ViewHolder>(ItemCallBack()) {

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
        val song = getItem(position)
        holder.song = song
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist
        Glide.with(holder.itemView.context)
            .load(song.thumbnail)
            .placeholder(R.drawable.ic_baseline_album)
            .centerInside()
            .into(holder.thumbnail)
    }

    class ViewHolder(itemView: View, onItemClick: ((Song) -> Unit)?) :
        RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.tvSongTitle
        val tvArtist: TextView = itemView.tvSongArtist
        val thumbnail: ImageView = itemView.imageThumbnail

        var song: Song? = null

        init {
            if (onItemClick != null) {
                itemView.setOnClickListener { song?.run { onItemClick(this) } }
            }
        }
    }

    companion object {
        class ItemCallBack : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem == newItem
            }
        }
    }
}
