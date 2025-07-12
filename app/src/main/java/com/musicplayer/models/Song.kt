package com.musicplayer.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumId: Long,
    val size: Long,
    val dateAdded: Long
) : Parcelable {
    fun getDurationFormatted(): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun getDisplayTitle(): String {
        return if (title.isNotBlank()) title else "Unknown Title"
    }
    
    fun getDisplayArtist(): String {
        return if (artist.isNotBlank()) artist else "Unknown Artist"
    }
    
    fun getDisplayAlbum(): String {
        return if (album.isNotBlank()) album else "Unknown Album"
    }
}