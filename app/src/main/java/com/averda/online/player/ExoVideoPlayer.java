package com.averda.online.player;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.averda.online.utils.Utils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExoVideoPlayer {
    private static final String TAG = "CAExoplayer";
    private PlayerView playerView;
    private SimpleExoPlayer videoPlayer;
    private boolean isAutoPlay;
    private Context context;
    public long startTime;
    public long endTime;

    public interface PlayerControl{
        void start();
        void complete();
        void buffering();
        void pause();
        void resume();
        void videoSize(int height, int width);
    }

    private PlayerControl playerControl;
    public ExoVideoPlayer(Context context, PlayerView playerView, boolean isController, boolean isAutoPlay){
        this.context = context;
        this.isAutoPlay = isAutoPlay;
        this.playerView = playerView;
        videoPlayer = ExoPlayerFactory.newSimpleInstance(context);
        playerView.setUseController(isController);
        playerView.setControlDispatcher(new DefaultControlDispatcher(){
            @Override
            public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
                if(playerControl != null){
                    if(playWhenReady){
                        playerControl.resume();
                    }else{
                        playerControl.pause();
                    }
                }
                return super.dispatchSetPlayWhenReady(player, playWhenReady);
            }
        });
        playerView.setPlayer(videoPlayer);

        videoPlayer.addVideoListener(new VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                if(playerControl!= null){
                    playerControl.videoSize(height, width);
                }
            }
        });

        videoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) { }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) { }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        if(playerControl != null){
                            playerControl.buffering();
                        }
                        break;
                    case Player.STATE_ENDED:
                        if(playerControl != null){
                            playerControl.complete();
                        }
                        break;
                    case Player.STATE_IDLE:

                        break;
                    case Player.STATE_READY:
                        if(playerControl != null){
                            playerControl.start();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            }

            @Override
            public void onSeekProcessed() {
            }
        });
    }

    public void setController(boolean isController){
        playerView.setUseController(isController);
    }

    public void setPlayer(String path){
        Uri uri = Uri.parse(path);
        MediaSource videoSource = new File(path).exists() ? new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(context, "Exoplayer-local")).
                createMediaSource(uri) : buildMediaSource(context, uri, null);
        if(endTime > 0){
            videoSource = new ClippingMediaSource(videoSource, startTime, endTime);
        }
        videoPlayer.prepare(videoSource);
        videoPlayer.setPlayWhenReady(isAutoPlay);
    }

    public void setResizeMode(int mode){
        playerView.setResizeMode(mode);
    }

    public void setPlayerControl(PlayerControl playerControl){
        this.playerControl = playerControl;
    }

    public void pauseVideo(){
        videoPlayer.setPlayWhenReady(false);
    }
    public void startVideo(){
        if(!isPlaying()) {
            videoPlayer.setPlayWhenReady(true);
        }
    }
    public void releasePlayer(){
        videoPlayer.release();
    }

    public long getCurrentPosition(){
        return videoPlayer.getCurrentPosition();
    }

    public long getDuration(){
        return videoPlayer.getDuration();
    }

    public long getBufferedPosition(){
        return videoPlayer.getBufferedPosition();
    }

    public boolean isPlaying(){
        return videoPlayer.getPlayWhenReady();
    }

    public void seekTo(long value){
        videoPlayer.seekTo(value);
    }

    public  MediaSource buildMediaSource(Context context, Uri uri, @Nullable String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        CacheDataSourceFactory cacheDataSourceFactory = new CacheDataSourceFactory(getDownloadCache(context), new DefaultHttpDataSourceFactory("ExoplayerDemo"));
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(cacheDataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    public static synchronized Cache getDownloadCache(Context context) {
        return VideoCache.getInstance(context);
    }
    public static Set<String> cacheMap = new HashSet<>();
    public static void cacheNextVideo(final Context context, final String path, String title){
        if(cacheMap.contains(path)){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(cacheMap.contains(path)){
                    return;
                }
                String videoUri = path;
                DataSpec dataSpec = new DataSpec(Uri.parse(videoUri), 0, 1 * 1024 * 1024, null);
                SimpleCache simpleCache = VideoCache.getInstance(context);
                CacheUtil.ProgressListener progressListener = new CacheUtil.ProgressListener() {
                    @Override
                    public void onProgress(long requestLength, long bytesCached, long newBytesCached) {
                        cacheMap.add(path);
                    }
                };
                try {
                    CacheUtil.cache(dataSpec, simpleCache, new CacheKeyFactory() {
                        @Override
                        public String buildCacheKey(DataSpec dataSpec) {
                            return dataSpec.key;
                        }
                    }, buildDataSourceFactory(context).createDataSource(), progressListener, new AtomicBoolean(false));
                } catch (Exception e) {
                    if(Utils.isDebugModeOn) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static DataSource.Factory buildDataSourceFactory(final Context context) {
        return new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                SimpleCache simpleCache = VideoCache.getInstance(context);

                DataSource dataSource = buildMyDataSourceFactory(context).createDataSource();
                return new CacheDataSource(simpleCache, dataSource);
            }
        };
    }
    private static DefaultDataSource.Factory buildMyDataSourceFactory(Context context) {
        return new CacheDataSourceFactory(VideoCache.getInstance(context), new DefaultHttpDataSourceFactory("ExoplayerDemo"));
    }

    public void setPlaybackSpeed(float value){
        PlaybackParameters param = new PlaybackParameters(value);
        videoPlayer.setPlaybackParameters(param);
    }
}
