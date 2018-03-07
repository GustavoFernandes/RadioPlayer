package com.github.gustavofernandes.radioplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AudioService extends MediaBrowserServiceCompat {

    private static final String TAG = AudioService.class.getSimpleName();
    private static final String ROOT = "root";

    private MediaSessionCompat mMediaSession;

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaSession = new MediaSessionCompat(this, TAG);
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setCallback(new MediaSessionCallback());

        setSessionToken(mMediaSession.getSessionToken());
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
        // TODO
    }
}
