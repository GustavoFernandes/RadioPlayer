package com.github.gustavofernandes.radioplayer;

import android.content.ComponentName;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private MediaBrowserCompat mMediaBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        @Override
        public void onConnected() {
            try {

                MediaControllerCompat mediaController = new MediaControllerCompat(
                        MainActivity.this,
                        mMediaBrowser.getSessionToken());

                mediaController.registerCallback(new MediaControllerCallback());

                MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

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
