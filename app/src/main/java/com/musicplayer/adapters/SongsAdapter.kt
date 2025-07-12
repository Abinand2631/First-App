package com.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.musicplayer.R
import com.musicplayer.databinding.ItemSongBinding
import com.musicplayer.models.Song
import com.musicplayer.utils.MediaScanner

class SongsAdapter(
    private val onSongClick: (Song, Int) -> Unit
) : ListAdapter<Song, SongsAdapter.SongViewHolder>(DiffCallback) {
    
    companion object DiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
    
    inner class SongViewHolder(private val binding: ItemSongBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(song: Song, position: Int) {
            binding.apply {
                textViewTitle.text = song.getDisplayTitle()
                textViewArtist.text = song.getDisplayArtist()
                textViewDuration.text = song.getDurationFormatted()
                
                // Load album art
                val albumArtUri = MediaScanner.getAlbumArtUri(song.albumId)
                Glide.with(imageViewAlbumArt.context)
                    .load(albumArtUri)
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .into(imageViewAlbumArt)
                
                root.setOnClickListener {
                    onSongClick(song, position)
                }
            }
        }
    }
}