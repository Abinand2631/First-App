package com.musicplayer.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.musicplayer.database.MusicDatabase
import com.musicplayer.models.Album
import com.musicplayer.models.Playlist
import com.musicplayer.models.PlaylistSong
import com.musicplayer.models.Song
import com.musicplayer.utils.MediaScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {
    private val database = MusicDatabase.getDatabase(context)
    private val playlistDao = database.playlistDao()
    private val playlistSongDao = database.playlistSongDao()
    
    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs
    
    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> = _albums
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    suspend fun refreshMusic() {
        _isLoading.postValue(true)
        try {
            val scannedSongs = MediaScanner.scanForSongs(context)
            val scannedAlbums = MediaScanner.scanForAlbums(context)
            
            _songs.postValue(scannedSongs)
            _albums.postValue(scannedAlbums)
        } finally {
            _isLoading.postValue(false)
        }
    }
    
    fun searchSongs(query: String): LiveData<List<Song>> {
        val filteredSongs = MutableLiveData<List<Song>>()
        val currentSongs = _songs.value ?: emptyList()
        
        if (query.isBlank()) {
            filteredSongs.value = currentSongs
        } else {
            val filtered = currentSongs.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
            }
            filteredSongs.value = filtered
        }
        
        return filteredSongs
    }
    
    // Playlist operations
    fun getAllPlaylists(): LiveData<List<Playlist>> = playlistDao.getAllPlaylists()
    
    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        val playlist = Playlist(name = name)
        playlistDao.insertPlaylist(playlist)
    }
    
    suspend fun deletePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlist)
    }
    
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        val playlistSong = PlaylistSong(playlistId = playlistId, songId = songId)
        playlistSongDao.addSongToPlaylist(playlistSong)
    }
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        playlistSongDao.removeSongFromPlaylist(playlistId, songId)
    }
    
    fun getSongsInPlaylist(playlistId: Long): LiveData<List<PlaylistSong>> =
        playlistSongDao.getSongsInPlaylist(playlistId)
    
    suspend fun getPlaylistSongs(playlistId: Long): List<Song> = withContext(Dispatchers.IO) {
        val songIds = playlistSongDao.getSongIdsInPlaylist(playlistId)
        val allSongs = _songs.value ?: emptyList()
        allSongs.filter { song -> songIds.contains(song.id) }
    }
}