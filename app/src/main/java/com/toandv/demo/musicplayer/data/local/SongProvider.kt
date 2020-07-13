package com.toandv.demo.musicplayer.data.local

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.toandv.demo.musicplayer.data.model.Song

object SongProvider {

    private const val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
    private val artworkUri = Uri.parse("content://media/external/audio/albumart")

    private val baseProjection = arrayOf(
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Albums.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Artists.ARTIST,
        MediaStore.Audio.Media.DURATION
    )

    fun getAllDeviceSongs(context: Context): List<Song> {
        val cursor =
            makeSongCursor(
                context
            )
        return getSongs(
            cursor
        )
    }

    private fun getSongs(cursor: Cursor?): List<Song> {

        val songs = mutableListOf<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                getSongFromCursor(
                    cursor
                ).let {
                    if (it.duration >= 20000) songs.add(it)
                }
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return songs
    }

    private fun getSongFromCursor(cursor: Cursor): Song {

        return cursor.run {
            val songTitle = getString(getColumnIndex(MediaStore.Audio.Media.TITLE))
            val songId = getString(getColumnIndex(MediaStore.Audio.Media._ID)).toLong()
            val songAlbum = getString(getColumnIndex(MediaStore.Audio.Albums.ALBUM))
            val songAlbumId = getString(getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toLong()
            val songArtist = getString(getColumnIndex(MediaStore.Audio.Artists.ARTIST))
            val songDuration = getString(getColumnIndex(MediaStore.Audio.Media.DURATION)).toLong()
            val albumArtUrl = ContentUris.withAppendedId(artworkUri, songAlbumId).toString()
            val songData =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
                    .toString()

            Song(songId, songTitle, songData, albumArtUrl, songAlbum, songArtist, songDuration)
        }
    }

    private fun makeSongCursor(context: Context): Cursor? {
        return try {
            context.run {
                contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    baseProjection,
                    selection, null, MediaStore.Audio.Media.TITLE
                )
            }
        } catch (e: SecurityException) {
            null
        }
    }
}
