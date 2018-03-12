package com.github.gustavofernandes.radioplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import java.util.*

class AudioService: MediaBrowserServiceCompat() {
    companion object {
        private val TAG = AudioService::class.java.simpleName
        const val ROOT = "root"
    }

    // TODO: optimize initialization of these members
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var playbackStateBuilder: PlaybackStateCompat.Builder
    private lateinit var audioManager: AudioManager
    private lateinit var audioManagerFocusChangeListener: AudioManagerFocusChangeListener
    private lateinit var noisyReceiver: BroadcastReceiver
    private lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(this, TAG)
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession.setCallback(MediaSessionCallback())

        sessionToken = mediaSession.sessionToken

        playbackStateBuilder = PlaybackStateCompat.Builder()
        playbackStateBuilder
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
                .setActions(PlaybackStateCompat.ACTION_PREPARE)

        mediaSession.setPlaybackState(playbackStateBuilder.build())

        mediaPlayer = MediaPlayer()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManagerFocusChangeListener = AudioManagerFocusChangeListener()

        noisyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // TODO: pause playback
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession.release()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(AudioService.ROOT, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(getMediaItemsById(parentId))
    }

    private fun getMediaItemsById(id: String): MutableList<MediaBrowserCompat.MediaItem>? {
        return Collections.emptyList() // TODO
    }

    private fun buildNotification() {
        notificationBuilder = MediaSessionNotificationBuilder.from(this, mediaSession)

        notificationBuilder.setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP)))
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            val result = audioManager.requestAudioFocus(audioManagerFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                mediaSession.isActive = true

                registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))

                // TODO: proceed with playing

                playbackStateBuilder
                        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)

                mediaSession.setPlaybackState(playbackStateBuilder.build())

                // TODO: set MediaMetadataCompat on mediaSession

                buildNotification() // TODO: optimize

                startForeground(1, notificationBuilder.build())
            }
        }

        override fun onPause() {
            mediaPlayer?.pause()

            playbackStateBuilder
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)

            mediaSession.setPlaybackState(playbackStateBuilder.build())

            stopForeground(false)
        }

        override fun onStop() {
            audioManager.abandonAudioFocus(audioManagerFocusChangeListener)
            mediaSession.isActive = false
            unregisterReceiver(noisyReceiver)
            stopForeground(true)
        }
    }

    private inner class AudioManagerFocusChangeListener : AudioManager.OnAudioFocusChangeListener {

        override fun onAudioFocusChange(i: Int) {
            if (i == AudioManager.AUDIOFOCUS_LOSS) {
                // TODO: implement this (stop playback here) and other changes
                // AUDIOFOCUS_LOSS_TRANSIENT, AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK, AUDIOFOCUS_GAIN
            }
        }
    }
}
