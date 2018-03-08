package com.github.gustavofernandes.radioplayer

import android.content.Context
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class MediaSessionNotificationBuilder {
    companion object {
        fun from(context: Context, mediaSession: MediaSessionCompat): NotificationCompat.Builder {
            val builder = NotificationCompat.Builder(context)

            val controller = mediaSession.controller
            val description = controller.metadata.description

            builder
                    .setContentTitle(description.title)
                    .setContentText(description.subtitle)
                    .setSubText(description.description)
                    .setLargeIcon(description.iconBitmap)

                    .setContentIntent(controller.sessionActivity)
                    .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))

                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            return builder
        }
    }
}