package com.musicplayer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.musicplayer.R
import com.musicplayer.activities.MainActivity
import com.musicplayer.models.Song

class MusicService : Service() {
    
    private val binder = MusicBinder()
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    
    private var currentSong: Song? = null
    private var playlist: List<Song> = emptyList()
    private var currentIndex: Int = 0
    private var isShuffleEnabled = false
    private var repeatMode = Player.REPEAT_MODE_OFF
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "MusicPlayerChannel"
        
        val isPlaying = MutableLiveData<Boolean>()
        val currentPosition = MutableLiveData<Long>()
        val duration = MutableLiveData<Long>()
        val currentSongLiveData = MutableLiveData<Song?>()
    }
    
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
    
    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        createNotificationChannel()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        isPlaying.postValue(exoPlayer.isPlaying)
                        duration.postValue(exoPlayer.duration)
                    }
                    Player.STATE_ENDED -> {
                        playNext()
                    }
                }
                updateNotification()
            }
            
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying.postValue(playing)
                updateNotification()
            }
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                currentPosition.postValue(exoPlayer.currentPosition)
            }
        })
        
        exoPlayer.repeatMode = repeatMode
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releaseAudioFocus()
        exoPlayer.release()
    }
    
    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        playlist = songs
        currentIndex = startIndex
        if (playlist.isNotEmpty()) {
            playSong(playlist[currentIndex])
        }
    }
    
    fun playSong(song: Song) {
        currentSong = song
        currentSongLiveData.postValue(song)
        
        if (requestAudioFocus()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(song.path))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }
    
    fun play() {
        if (currentSong != null && requestAudioFocus()) {
            exoPlayer.play()
        }
    }
    
    fun pause() {
        exoPlayer.pause()
    }
    
    fun stop() {
        exoPlayer.stop()
        stopForeground(true)
        releaseAudioFocus()
    }
    
    fun playNext() {
        if (playlist.isNotEmpty()) {
            if (isShuffleEnabled) {
                currentIndex = (0 until playlist.size).random()
            } else {
                currentIndex = (currentIndex + 1) % playlist.size
            }
            playSong(playlist[currentIndex])
        }
    }
    
    fun playPrevious() {
        if (playlist.isNotEmpty()) {
            if (isShuffleEnabled) {
                currentIndex = (0 until playlist.size).random()
            } else {
                currentIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
            }
            playSong(playlist[currentIndex])
        }
    }
    
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        currentPosition.postValue(position)
    }
    
    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
    }
    
    fun setRepeatMode(mode: Int) {
        repeatMode = mode
        exoPlayer.repeatMode = mode
    }
    
    fun getCurrentPosition(): Long = exoPlayer.currentPosition
    fun getDuration(): Long = exoPlayer.duration
    fun isPlaying(): Boolean = exoPlayer.isPlaying
    
    private fun requestAudioFocus(): Boolean {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            
            audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }
    
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }
    
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                exoPlayer.volume = 1.0f
                play()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                exoPlayer.volume = 0.3f
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                stop()
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(currentSong?.getDisplayTitle() ?: "Unknown")
        .setContentText(currentSong?.getDisplayArtist() ?: "Unknown Artist")
        .setSmallIcon(R.drawable.ic_music_note)
        .setContentIntent(createContentIntent())
        .setDeleteIntent(createDeleteIntent())
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .addAction(createPreviousAction())
        .addAction(createPlayPauseAction())
        .addAction(createNextAction())
        .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0, 1, 2))
        .build()
    
    private fun updateNotification() {
        if (currentSong != null) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }
    }
    
    private fun createContentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createDeleteIntent(): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            action = "STOP"
        }
        return PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createPlayPauseAction(): NotificationCompat.Action {
        val icon = if (exoPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val title = if (exoPlayer.isPlaying) "Pause" else "Play"
        val intent = Intent(this, MusicService::class.java).apply {
            action = "PLAY_PAUSE"
        }
        val pendingIntent = PendingIntent.getService(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(icon, title, pendingIntent)
    }
    
    private fun createPreviousAction(): NotificationCompat.Action {
        val intent = Intent(this, MusicService::class.java).apply {
            action = "PREVIOUS"
        }
        val pendingIntent = PendingIntent.getService(
            this, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(R.drawable.ic_skip_previous, "Previous", pendingIntent)
    }
    
    private fun createNextAction(): NotificationCompat.Action {
        val intent = Intent(this, MusicService::class.java).apply {
            action = "NEXT"
        }
        val pendingIntent = PendingIntent.getService(
            this, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(R.drawable.ic_skip_next, "Next", pendingIntent)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY_PAUSE" -> if (exoPlayer.isPlaying) pause() else play()
            "PREVIOUS" -> playPrevious()
            "NEXT" -> playNext()
            "STOP" -> stop()
        }
        return START_STICKY
    }
}