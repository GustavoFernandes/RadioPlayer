package com.github.gustavofernandes.radioplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

@Suppress("DEPRECATION")
class MediaSessionWrapper(context: Context, callback: MediaSessionCallback) : AudioManager.OnAudioFocusChangeListener {

    companion object {
        private val TAG = MediaSessionWrapper::class.java.simpleName
    }

    val mediaSession = MediaSessionCompat(context, TAG)
    val sessionToken: MediaSessionCompat.Token

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            if (!canPlay()) return

            mediaSession.isActive = true

            context.registerReceiver(
                    noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))

            playbackStateBuilder
                    .setState(PlaybackStateCompat.STATE_PLAYING,0, 1f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
            mediaSession.setPlaybackState(playbackStateBuilder.build())

            callback.onPlay()
        }

        override fun onPause() {
            playbackStateBuilder
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
            mediaSession.setPlaybackState(playbackStateBuilder.build())

            callback.onPause()
        }

        override fun onStop() {
            mediaSession.isActive = false

            audioManager.abandonAudioFocus(this@MediaSessionWrapper)

            context.unregisterReceiver(noisyReceiver)

            callback.onStop()
        }
    }

    private val playbackStateBuilder = PlaybackStateCompat.Builder()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            // TODO: pause playback
        }
    }

    init {
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mediaSession.setCallback(mediaSessionCallback)

        sessionToken = mediaSession.sessionToken

        playbackStateBuilder
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
                .setActions(PlaybackStateCompat.ACTION_PREPARE)
        mediaSession.setPlaybackState(playbackStateBuilder.build())
    }

    fun onDestroy() {
        mediaSession.release()
    }

    private fun canPlay(): Boolean {
        val result = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun onAudioFocusChange(i: Int) {
        /* TODO
        AudioManager.AUDIOFOCUS_LOSS
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
        AudioManager.AUDIOFOCUS_GAIN
        */
    }
}
