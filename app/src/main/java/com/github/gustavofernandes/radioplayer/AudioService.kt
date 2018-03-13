package com.github.gustavofernandes.radioplayer

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaButtonReceiver
import java.util.*

class AudioService: MediaBrowserServiceCompat(), MediaSessionCallback {

    companion object {
        const val ROOT = "root"
    }

    private lateinit var mediaSessionWrapper: MediaSessionWrapper

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()

        mediaSessionWrapper = MediaSessionWrapper(this, this)

        sessionToken = mediaSessionWrapper.sessionToken

        mediaPlayer = MediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSessionWrapper.mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaSessionWrapper.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(ROOT, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(getMediaItemsById(parentId))
    }

    override fun onPlay() {
        // TODO: play mediaplayer
        startForeground(1, mediaSessionWrapper.notificationBuilder.build())
    }

    override fun onPause() {
        // TODO: pause mediaplayer
        stopForeground(false)
    }

    override fun onStop() {
        stopForeground(true)
    }

    private fun getMediaItemsById(id: String): MutableList<MediaBrowserCompat.MediaItem>? {
        return Collections.emptyList() // TODO
    }
}
