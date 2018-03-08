package com.github.gustavofernandes.radioplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.Collections;
import java.util.List;

public class AudioService extends MediaBrowserServiceCompat {

    public static final String ROOT = "root";

    private static final String TAG = AudioService.class.getSimpleName();

    private MediaSessionCompat mMediaSession;
    private MediaPlayer mMediaPlayer;

    private AudioManager mAudioManager;
    private AudioManagerFocusChangeListener mAudioManagerFocusChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaSession = new MediaSessionCompat(this, TAG);
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setCallback(new MediaSessionCallback());

        setSessionToken(mMediaSession.getSessionToken());

        mMediaPlayer = new MediaPlayer();

        // TODO: move AudioManager and listener to own class?
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManagerFocusChangeListener = new AudioManagerFocusChangeListener();
    }
 
    @Override
    public void onDestroy() {
        mMediaPlayer.release();
        mMediaPlayer = null;
        mMediaSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(getMediaItemsById(parentId));
    }

    private List<MediaBrowserCompat.MediaItem> getMediaItemsById(String id) {
        // TODO

        return Collections.emptyList();
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        // TODO: set MediaSession to active/inactive


        @Override
        public void onPlay() {
            int result = mAudioManager.requestAudioFocus(mAudioManagerFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // TODO: proceed with playing
            }
        }

        @Override
        public void onPause() {
            mMediaPlayer.pause();
        }

        @Override
        public void onStop() {
            mAudioManager.abandonAudioFocus(mAudioManagerFocusChangeListener);
        }
    }

    private class AudioManagerFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int i) {
            if (i == AudioManager.AUDIOFOCUS_LOSS) {
                // TODO: implement this (stop playback here) and other changes
                // AUDIOFOCUS_LOSS_TRANSIENT, AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK, AUDIOFOCUS_GAIN
            }
        }
    }
}
