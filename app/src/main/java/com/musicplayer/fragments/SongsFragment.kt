package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.activities.MainActivity
import com.musicplayer.adapters.SongsAdapter
import com.musicplayer.databinding.FragmentSongsBinding
import com.musicplayer.models.Song
import com.musicplayer.viewmodels.MusicViewModel

class SongsFragment : Fragment() {
    
    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MusicViewModel
    private lateinit var songsAdapter: SongsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]
        setupRecyclerView()
        setupSearchView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        songsAdapter = SongsAdapter { song, position ->
            onSongClick(song, position)
        }
        
        binding.recyclerViewSongs.apply {
            adapter = songsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchSongs(newText ?: "")
                return true
            }
        })
    }
    
    private fun observeViewModel() {
        viewModel.songs.observe(viewLifecycleOwner) { songs ->
            if (viewModel.searchQuery.value.isNullOrBlank()) {
                songsAdapter.submitList(songs)
            }
            updateEmptyState(songs.isEmpty())
        }
        
        viewModel.filteredSongs.observe(viewLifecycleOwner) { songs ->
            if (!viewModel.searchQuery.value.isNullOrBlank()) {
                songsAdapter.submitList(songs)
                updateEmptyState(songs.isEmpty())
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.textViewEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewSongs.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun onSongClick(song: Song, position: Int) {
        val activity = requireActivity() as MainActivity
        val musicService = activity.getMusicService()
        
        if (musicService != null) {
            val songs = if (viewModel.searchQuery.value.isNullOrBlank()) {
                viewModel.songs.value ?: emptyList()
            } else {
                viewModel.filteredSongs.value ?: emptyList()
            }
            
            musicService.setPlaylist(songs, position)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}