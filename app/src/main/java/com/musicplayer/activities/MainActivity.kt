package com.musicplayer.activities

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.musicplayer.R
import com.musicplayer.databinding.ActivityMainBinding
import com.musicplayer.fragments.AlbumsFragment
import com.musicplayer.fragments.PlaylistsFragment
import com.musicplayer.fragments.SongsFragment
import com.musicplayer.services.MusicService
import com.musicplayer.utils.PermissionManager
import com.musicplayer.viewmodels.MusicViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MusicViewModel
    private var musicService: MusicService? = null
    private var isServiceBound = false
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isServiceBound = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isServiceBound = false
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.refreshMusic()
        } else {
            showPermissionDeniedDialog()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        
        setupBottomNavigation()
        checkPermissions()
        bindMusicService()
        
        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(SongsFragment())
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_songs -> {
                    loadFragment(SongsFragment())
                    true
                }
                R.id.nav_albums -> {
                    loadFragment(AlbumsFragment())
                    true
                }
                R.id.nav_playlists -> {
                    loadFragment(PlaylistsFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    private fun checkPermissions() {
        if (!PermissionManager.hasAudioPermission(this)) {
            if (PermissionManager.shouldShowRationale(this)) {
                showPermissionRationaleDialog()
            } else {
                requestPermissions()
            }
        }
    }
    
    private fun requestPermissions() {
        permissionLauncher.launch(PermissionManager.getRequiredPermissions())
    }
    
    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.permission_message)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                requestPermissions()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }
    
    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_required)
            .setMessage("The app cannot function without music file access permissions.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .show()
    }
    
    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
    
    fun getMusicService(): MusicService? = musicService
}