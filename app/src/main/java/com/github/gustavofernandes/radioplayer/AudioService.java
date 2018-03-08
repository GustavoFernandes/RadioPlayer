package com.github.gustavofernandes.radioplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    private BroadcastReceiver mNoisyReceiver;

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

        mNoisyReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: pause playback
            }
        };
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

        @Override
        public void onPlay() {
            int result = mAudioManager.requestAudioFocus(mAudioManagerFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                mMediaSession.setActive(true);

                registerReceiver(mNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

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
            mMediaSession.setActive(false);
            unregisterReceiver(mNoisyReceiver);
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
