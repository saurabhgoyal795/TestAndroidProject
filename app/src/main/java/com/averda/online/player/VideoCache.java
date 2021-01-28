package com.averda.online.player;

import android.content.Context;

import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

public class VideoCache {
    private static SimpleCache sDownloadCache;
    public static SimpleCache getInstance(Context context) {
        if (sDownloadCache == null){
            sDownloadCache = new SimpleCache(new File(context.getFilesDir(), "VideoCache"), new LeastRecentlyUsedCacheEvictor(200*1024*1024));
        }
        return sDownloadCache;
    }
}
