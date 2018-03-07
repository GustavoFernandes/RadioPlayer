package com.github.gustavofernandes.radioplayer;

import android.content.ComponentName;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private MediaBrowserCompat mMediaBrowser;

    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private Button mPreviousButton;
    private Button mPlayPauseButton;
    private Button mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitleTextView = findViewById(R.id.textview_title);
        mArtistTextView = findViewById(R.id.textview_artist);
        mPreviousButton = findViewById(R.id.button_previous);
        mPlayPauseButton = findViewById(R.id.button_playPause);
        mNextButton = findViewById(R.id.button_next);

        mMediaBrowser = new MediaBrowserCompat(
                this,
                new ComponentName(this, AudioService.class),
                new MediaBrowserConnectionCallback(),
                null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    private void buildTransportControls() {

        // TODO: make TransportControls function-wide?

        mPreviousButton.setOnClickListener(view -> MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToPrevious());
        mNextButton.setOnClickListener(view -> MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToNext());

        mPlayPauseButton.setOnClickListener(view -> {
            if (MediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
            } else {
                MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
            }
        });
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        @Override
        public void onConnected() {
            try {

                MediaControllerCompat mediaController = new MediaControllerCompat(
                        MainActivity.this,
                        mMediaBrowser.getSessionToken());

                mediaController.registerCallback(new MediaControllerCallback());

                MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

                buildTransportControls();

            } catch (RemoteException e) {
                // TODO
                e.printStackTrace();
            }
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            // TODO
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            // TODO
        }
    }
}
