package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.musicplayer.adapters.AlbumAdapter
import com.musicplayer.databinding.FragmentAlbumsBinding
import com.musicplayer.viewmodels.MusicViewModel

class AlbumsFragment : Fragment() {
    
    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MusicViewModel
    private lateinit var albumAdapter: AlbumAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        albumAdapter = AlbumAdapter { album ->
            // Handle album click
        }
        
        binding.recyclerViewAlbums.apply {
            adapter = albumAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }
    
    private fun observeViewModel() {
        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            albumAdapter.submitList(albums)
            updateEmptyState(albums.isEmpty())
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.textViewEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewAlbums.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}