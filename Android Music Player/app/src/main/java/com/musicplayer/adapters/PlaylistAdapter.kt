package com.musicplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.musicplayer.databinding.ItemPlaylistBinding
import com.musicplayer.models.Playlist

class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(DiffCallback) {
    
    companion object DiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(playlist: Playlist) {
            binding.apply {
                textViewPlaylistName.text = playlist.name
                textViewSongCount.text = "${playlist.songCount} songs"
                
                root.setOnClickListener {
                    onPlaylistClick(playlist)
                }
            }
        }
    }
}