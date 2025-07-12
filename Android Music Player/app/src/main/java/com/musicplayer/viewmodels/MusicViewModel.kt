package com.musicplayer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.musicplayer.models.Album
import com.musicplayer.models.Playlist
import com.musicplayer.models.Song
import com.musicplayer.repositories.MusicRepository
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(application)
    
    val songs: LiveData<List<Song>> = repository.songs
    val albums: LiveData<List<Album>> = repository.albums
    val isLoading: LiveData<Boolean> = repository.isLoading
    val playlists: LiveData<List<Playlist>> = repository.getAllPlaylists()
    
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery
    
    private val _filteredSongs = MutableLiveData<List<Song>>()
    val filteredSongs: LiveData<List<Song>> = _filteredSongs
    
    init {
        refreshMusic()
    }
    
    fun refreshMusic() {
        viewModelScope.launch {
            repository.refreshMusic()
        }
    }
    
    fun searchSongs(query: String) {
        _searchQuery.value = query
        val currentSongs = songs.value ?: emptyList()
        
        if (query.isBlank()) {
            _filteredSongs.value = currentSongs
        } else {
            val filtered = currentSongs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
            }
            _filteredSongs.value = filtered
        }
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _filteredSongs.value = songs.value ?: emptyList()
    }
    
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }
    
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }
    
    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }
    
    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }
    
    fun getPlaylistSongs(playlistId: Long): LiveData<List<Song>> {
        val playlistSongs = MutableLiveData<List<Song>>()
        viewModelScope.launch {
            val songs = repository.getPlaylistSongs(playlistId)
            playlistSongs.postValue(songs)
        }
        return playlistSongs
    }
}