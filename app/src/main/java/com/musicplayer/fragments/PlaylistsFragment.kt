package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.musicplayer.adapters.PlaylistAdapter
import com.musicplayer.databinding.FragmentPlaylistsBinding
import com.musicplayer.viewmodels.MusicViewModel

class PlaylistsFragment : Fragment() {
    
    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MusicViewModel
    private lateinit var playlistAdapter: PlaylistAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter { playlist ->
            // Handle playlist click
        }
        
        binding.recyclerViewPlaylists.apply {
            adapter = playlistAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    
    private fun observeViewModel() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.submitList(playlists)
            updateEmptyState(playlists.isEmpty())
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.textViewEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewPlaylists.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}