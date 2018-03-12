package com.github.gustavofernandes.radioplayer

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mediaBrowser: MediaBrowserCompat

    private var isPlaying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaBrowser = MediaBrowserCompat(
                this,
                ComponentName(this, AudioService::class.java),
                MediaBrowserConnectionCallback(),
                null)
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    private fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(this@MainActivity)
        val transportControls = mediaController.transportControls

        button_previous.setOnClickListener { transportControls.skipToPrevious() }
        button_next.setOnClickListener { transportControls.skipToNext() }
        button_playPause.setOnClickListener {
            if (isPlaying) {
                transportControls.pause()
            } else {
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

    private inner class MediaBrowserConnectionCallback : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            val mediaController = MediaControllerCompat(
                    this@MainActivity,
                    mediaBrowser.sessionToken)

            mediaController.registerCallback(MediaControllerCallback())

            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)

            buildTransportControls()

            mediaBrowser.subscribe(AudioService.ROOT, MediaBrowserSubscriptionCallback())
        }
    }

    private class MediaBrowserSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
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
