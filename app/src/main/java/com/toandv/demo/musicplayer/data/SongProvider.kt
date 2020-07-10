package com.toandv.demo.musicplayer.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

object SongProvider {

    private const val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
    private val artworkUri = Uri.parse("content://media/external/audio/albumart")
    private val allDeviceSongs = ArrayList<Song>()

    private val baseProjection = arrayOf(
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION
    )

    fun getAllDeviceSongs(context: Context): MutableList<Song> {
        val cursor =
            makeSongCursor(context)
        return getSongs(cursor)
    }

    private fun getSongs(cursor: Cursor?): MutableList<Song> {

        val songs = ArrayList<Song>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val song =
                    getSongFromCursorImpl(
                        cursor
                    )
                if (song.duration >= 20000) {
                    songs.add(song)
                    allDeviceSongs.add(song)
                }
            } while (cursor.moveToNext())
        }
        cursor?.close()

        return songs
    }


    private fun getSongFromCursorImpl(cursor: Cursor): Song {

        val id = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
        val title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val album = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
        val albumId = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
        val artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        val duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

        val songTitle = cursor.getString(title)
        val songId = cursor.getString(id).toLong()
        val songAlbum = cursor.getString(album)
        val songAlbumId = cursor.getString(albumId).toLong()
        val songArtist = cursor.getString(artist)
        val songDuration = cursor.getString(duration).toLong()
        val albumArtUrl = ContentUris.withAppendedId(artworkUri, songAlbumId).toString()
        val songData =
            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
                .toString()


        return Song(songId, songTitle, songData, albumArtUrl, songAlbum, songArtist, songDuration)
    }

    private fun makeSongCursor(context: Context): Cursor? {
        try {
            return context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                baseProjection,
                selection, null, MediaStore.Audio.Media.TITLE
            )
        } catch (e: SecurityException) {
            return null
        }
    }
}
