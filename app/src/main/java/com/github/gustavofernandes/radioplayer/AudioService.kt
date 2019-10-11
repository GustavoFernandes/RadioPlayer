package com.github.gustavofernandes.radioplayer

import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.util.Log
import java.util.*

class AudioService: MediaBrowserServiceCompat(), MediaSessionCallback {

    companion object {
        const val ROOT = "root"
        const val STATIONS = "stations"
    }

    private val TAG = AudioService::class.java.simpleName

    private lateinit var mediaSessionWrapper: MediaSessionWrapper

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        mediaSessionWrapper = MediaSessionWrapper(this, this)

        sessionToken = mediaSessionWrapper.sessionToken

        mediaPlayer = MediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        MediaButtonReceiver.handleIntent(mediaSessionWrapper.mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        mediaSessionWrapper.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        Log.d(TAG, "onGetRoot")
        return BrowserRoot(ROOT, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        Log.d(TAG, "onLoadChildren")

        val list = mutableListOf<MediaBrowserCompat.MediaItem>()

        if (parentId == STATIONS) {
            val cursor: Cursor? = contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Audio.Media.DATA + " LIKE ?",
                    arrayOf("%/radioplayer-media/Stations/%"),
                    MediaStore.Audio.Media.DATA)

            when {
                cursor == null -> {
                    Log.d(TAG, "query failed")
                }
                !cursor.moveToFirst() -> {
                    Log.d(TAG, "no media found")
                }
                else -> {
                    var prevStationTitle: String? = null
                    do {
                        val absolutePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                        val stationIndex = absolutePath.indexOf("/radioplayer-media/Stations/") + "/radioplayer-media/Stations/".length
                        val stationTitle = absolutePath.substring(stationIndex, absolutePath.indexOf('/', stationIndex))

                        if (stationTitle != prevStationTitle) {

                            Log.d(TAG, "Found station titled: $stationTitle")

                            val mediaDescription = MediaDescriptionCompat.Builder()
                                    .setMediaId("$STATIONS - $stationTitle") // TODO: change mediaId to something more unique
                                    .setTitle(stationTitle)
                                    .build()

                            list.add(MediaBrowserCompat.MediaItem(mediaDescription, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE))
                            prevStationTitle = stationTitle
                        }

                    } while (cursor.moveToNext())
                }
            }

            cursor?.close()
        }

        result.sendResult(list)
    }

    override fun onPlay() {
        Log.d(TAG, "onPlay")
        // TODO: play mediaplayer
        startForeground(1, mediaSessionWrapper.notificationBuilder.build())
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        // TODO: pause mediaplayer
        stopForeground(false)
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        stopForeground(true)
    }

    private fun getMediaItemsById(id: String): MutableList<MediaBrowserCompat.MediaItem>? {
        return Collections.emptyList() // TODO
    }
}
