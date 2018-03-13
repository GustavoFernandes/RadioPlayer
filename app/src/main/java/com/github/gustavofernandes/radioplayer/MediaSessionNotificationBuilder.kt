package com.github.gustavofernandes.radioplayer

import android.content.Context
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

@Suppress("DEPRECATION")
class MediaSessionNotificationBuilder(private val context: Context?) : NotificationCompat.Builder(context) {
    fun from(mediaSession: MediaSessionCompat): NotificationCompat.Builder {

        val controller = mediaSession.controller
        val description = controller?.metadata?.description

        if (description === null) return this // TODO

        val mediaStyle = android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context, PlaybackStateCompat.ACTION_STOP))

        this
                .setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setSubText(description.description)
                .setLargeIcon(description.iconBitmap)

                .setContentIntent(controller.sessionActivity)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))

                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                .setStyle(mediaStyle)

        return this
    }
}