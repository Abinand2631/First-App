package com.musicplayer.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.musicplayer.models.Album
import com.musicplayer.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaScanner {
    
    suspend fun scanForSongs(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        if (!PermissionManager.hasAudioPermission(context)) {
            return@withContext songs
        }
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = ? AND ${MediaStore.Audio.Media.DURATION} > ?"
        val selectionArgs = arrayOf("1", "30000") // Only music files longer than 30 seconds
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            
            while (cursor.moveToNext()) {
                val song = Song(
                    id = cursor.getLong(idColumn),
                    title = cursor.getString(titleColumn) ?: "",
                    artist = cursor.getString(artistColumn) ?: "",
                    album = cursor.getString(albumColumn) ?: "",
                    duration = cursor.getLong(durationColumn),
                    path = cursor.getString(dataColumn) ?: "",
                    albumId = cursor.getLong(albumIdColumn),
                    size = cursor.getLong(sizeColumn),
                    dateAdded = cursor.getLong(dateAddedColumn)
                )
                songs.add(song)
            }
        }
        
        songs
    }
    
    suspend fun scanForAlbums(context: Context): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()
        
        if (!PermissionManager.hasAudioPermission(context)) {
            return@withContext albums
        }
        
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR
        )
        
        val sortOrder = "${MediaStore.Audio.Albums.ALBUM} ASC"
        
        context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val songCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)
            
            while (cursor.moveToNext()) {
                val album = Album(
                    id = cursor.getLong(idColumn),
                    name = cursor.getString(albumColumn) ?: "",
                    artist = cursor.getString(artistColumn) ?: "",
                    songCount = cursor.getInt(songCountColumn),
                    year = cursor.getInt(yearColumn)
                )
                albums.add(album)
            }
        }
        
        albums
    }
    
    fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }
}