package com.averda.online.player;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.averda.online.BuildConfig;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.utils.Utils;

public class YoutubePlayerActivity extends ZTAppCompatActivity  implements YouTubePlayer.OnInitializedListener{
    private YouTubePlayerSupportFragment youTubePlayerSupportFragment;
    private YouTubePlayer player;
    private String youtube_id;
    private int currentTime;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_youtube_player);
        Bundle bundle = getIntent().getExtras();
        youtube_id = bundle.getString("youtubeId");
        youTubePlayerSupportFragment = new YouTubePlayerSupportFragment();
        youTubePlayerSupportFragment.initialize(BuildConfig.YT_DEV_KEY, this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.youtube_view, youTubePlayerSupportFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
        youTubePlayer.setManageAudioFocus(true);
        youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                |YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                |YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        try{
            if (!b) {
                youTubePlayer.loadVideo(youtube_id);
            }
            else {
                youTubePlayer.play();
            }
            player = youTubePlayer;
        }catch (Throwable e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(String s) {
                try {
                    if (player != null) {
                        if(currentTime != 0){
                            player.seekToMillis(currentTime);
                            player.pause();
                        } else {
                            if (player != null) {
                                player.pause();
                            }
                        }
                    }

                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
                if (player != null) {
                    int currentPosition = player.getCurrentTimeMillis();
                    if (currentPosition == 0) {
                        player.play();
                    } else {
                        player.pause();
                    }
                }
            }

            @Override
            public void onAdStarted() {
            }

            @Override
            public void onVideoStarted() {
            }

            @Override
            public void onVideoEnded() {
                youTubePlayer.seekToMillis(0);
                youTubePlayer.pause();
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
            }
        });
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
}
