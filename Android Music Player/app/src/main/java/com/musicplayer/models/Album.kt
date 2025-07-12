package com.musicplayer.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val year: Int
) : Parcelable {
    fun getDisplayName(): String {
        return if (name.isNotBlank()) name else "Unknown Album"
    }
    
    fun getDisplayArtist(): String {
        return if (artist.isNotBlank()) artist else "Unknown Artist"
    }
}