package com.github.gustavofernandes.radioplayer

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private lateinit var mediaBrowser: MediaBrowserCompat

    private var isPlaying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaBrowser = MediaBrowserCompat(
                this,
                ComponentName(this, AudioService::class.java),
                MediaBrowserConnectionCallback(),
                null)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }
    }

    override fun onStart() {
        Log.d(TAG, "onStart")

        super.onStart()

        Log.d(TAG, "Media browser connecting...")
        mediaBrowser.connect()
    }

    private fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(this@MainActivity)
        val transportControls = mediaController.transportControls

        button_previous.setOnClickListener { transportControls.skipToPrevious() }
        button_next.setOnClickListener { transportControls.skipToNext() }
        button_playPause.setOnClickListener {
            if (isPlaying) {
                Log.d(TAG, "Pause button clicked")
                transportControls.pause()
            } else {
                Log.d(TAG, "Play button clicked")
                transportControls.play()
            }
        }

        updatePlayPauseView()
        updateTitleAndArtistViews(mediaController.metadata)
    }

    private fun updatePlayPauseView() {
        // TODO: account for states other than STATE_PLAYING or not"
        button_playPause.text = if (isPlaying) "PAUSE" else "PLAY"
    }

    private fun updateTitleAndArtistViews(metadata: MediaMetadataCompat?) {
        if (metadata != null) {
            textview_title.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            textview_artist.text = metadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST)
        }
    }

    private fun loadStations() {
        Log.d(TAG, "Loading stations...")

        mediaBrowser.subscribe(AudioService.STATIONS, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
                Log.d(TAG, "Stations loaded: $children")

            }
        })
    }

    private inner class MediaBrowserConnectionCallback : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            Log.d(TAG, "Media browser connected")

            val mediaController = MediaControllerCompat(
                    this@MainActivity,
                    mediaBrowser.sessionToken)

            mediaController.registerCallback(MediaControllerCallback())

            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)

            buildTransportControls()
            loadStations()
        }

        override fun onConnectionSuspended() {
            // TODO
        }

        override fun onConnectionFailed() {
            // TODO
        }
    }

    private inner class MediaBrowserSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
            Log.d("MediaBrowserSubCallback", "Media browser onChildrenLoaded. parentId:$parentId, children:$children")
            // TODO
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            updateTitleAndArtistViews(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            isPlaying = state != null && state.state == PlaybackStateCompat.STATE_PLAYING
            updatePlayPauseView()
        }
    }
}
