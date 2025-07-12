package com.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.musicplayer.R
import com.musicplayer.databinding.ItemAlbumBinding
import com.musicplayer.models.Album
import com.musicplayer.utils.MediaScanner

class AlbumAdapter(
    private val onAlbumClick: (Album) -> Unit
) : ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(DiffCallback) {
    
    companion object DiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class AlbumViewHolder(private val binding: ItemAlbumBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(album: Album) {
            binding.apply {
                textViewAlbumName.text = album.getDisplayName()
                textViewArtistName.text = album.getDisplayArtist()
                textViewSongCount.text = "${album.songCount} songs"
                
                // Load album art
                val albumArtUri = MediaScanner.getAlbumArtUri(album.id)
                Glide.with(imageViewAlbumArt.context)
                    .load(albumArtUri)
                    .placeholder(R.drawable.ic_album)
                    .error(R.drawable.ic_album)
                    .into(imageViewAlbumArt)
                
                root.setOnClickListener {
                    onAlbumClick(album)
                }
            }
        }
    }
}