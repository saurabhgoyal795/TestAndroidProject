package com.averda.online.player;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerActivity extends ZTAppCompatActivity {

    private final static String TAG = "CAVideoPlayerActivity";
    String videoId = "";
    String videoTitle;
    private PlayerView nativePlayer;
    private ProgressBar videoProgress;
    private ExoVideoPlayer videoPlayer;
    private String videoUrl;
    private HashMap<Integer, HashMap<String, Object>> qualityMaps;
    private ArrayList<Integer> qualityList;
    private DisplayMetrics metrics;
    private String thumbPath;
    private int videoWidth;
    private int videoHeight;
    private long currentTime;
    private ImageView mFullScreenIcon;
    private ImageView exoSetting;
    int selectedQuality = -1;
    int selectedSpeed = 0;
    private View mContentView;
    private ArrayList<String> speedOptions = new ArrayList<>();
    private Float[] speedValues = {1f, 1.25f, 1.5f, 1.75f, 2f, 2.5f, 3f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_video_player_new);
        mContentView = findViewById(R.id.rootView);
        nativePlayer = findViewById(R.id.nativePlayer);
        videoProgress = findViewById(R.id.videoProgress);
        PlayerControlView controlView = nativePlayer.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        exoSetting = controlView.findViewById(R.id.exo_setting);
        metrics = Utils.getMetrics(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            if(Utils.isLollipop()) {
                int position = bundle.getInt("position");
                findViewById(R.id.image).setTransitionName("video_"+position);
            }
            videoId = bundle.getString("videoUrl");
            videoTitle = bundle.getString("title");
            setTitle(videoTitle);
        }
        videoWidth = metrics.widthPixels;
        videoHeight = (int)((videoWidth * 1f)/1.77f);
        findViewById(R.id.image).getLayoutParams().height = findViewById(R.id.nativePlayer).getLayoutParams().height = videoHeight;
        thumbPath = VimeoPlayer.getVideoImagePath(this, videoId);
        if(Utils.isValidString(thumbPath)){
            setImage();
        }
        VimeoPlayer.getVideo(this, videoId, new VimeoPlayer.FetchListener() {
            @Override
            public void videoData(HashMap<String, Object> fetchedData) {
                if (Utils.isActivityDestroyed(PlayerActivity.this)) {
                    return;
                }
                if(fetchedData != null){
                    setPlayerValues(fetchedData);
                }else{
                    openWebPlayer();
                }
            }

            @Override
            public void error(String error) {
                openWebPlayer();
            }
        });
        setSpeedOptions();
    }

    private void setSpeedOptions(){
        for (int i = 0 ; i < speedValues.length ; i++){
            speedOptions.add(speedValues[i]+"x");
        }
    }

    private void setPlayerValues(HashMap<String, Object> fetchedData){
        qualityMaps = (HashMap<Integer, HashMap<String, Object>>) fetchedData.get("qualityMaps");
        qualityList = (ArrayList<Integer>)fetchedData.get("qualityList");
        thumbPath = (String)fetchedData.get("thumbPath");
        setVideoLayout();
        setImage();
        int position = qualityList.indexOf(360);
        if(position < 0){
            position = qualityList.size() - 1;
        }
        setQuality(position);
    }

    private void openWebPlayer(){
        Intent intent = new Intent(PlayerActivity.this, WebVideoPlayer.class);
        intent.putExtra("videoUrl", videoUrl);
        intent.putExtra("title", videoTitle);
        startActivity(intent);
        finish();
    }

    private void playNativePlayer(){
        videoProgress.setVisibility(View.VISIBLE);
        nativePlayer.setVisibility(View.VISIBLE);
        videoPlayer = new ExoVideoPlayer(this, nativePlayer, true, true);
        videoPlayer.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        videoPlayer.setPlayerControl(new ExoVideoPlayer.PlayerControl() {
            @Override
            public void start() {
                if(videoProgress.getVisibility() == View.VISIBLE){
                    videoPlayer.seekTo(currentTime);
                    findViewById(R.id.image).setVisibility(View.GONE);
                }
                videoProgress.setVisibility(View.GONE);
            }

            @Override
            public void complete() {
                videoPlayer.seekTo(0);
                videoPlayer.pauseVideo();
            }

            @Override
            public void buffering() {

            }

            @Override
            public void pause() {

            }

            @Override
            public void resume() {

            }

            @Override
            public void videoSize(int height, int width) {

            }
        });

        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Utils.isValidString(videoUrl)) {
                    videoPlayer.setPlayer(videoUrl);
                }
            }
        },1000);
        if (Utils.isLandscape(this)) {
            mFullScreenIcon.setImageResource(R.drawable.fullscreen_exit_white_18dp);
        } else {
            mFullScreenIcon.setImageResource(R.drawable.fullscreen_white_18dp);
        }
        mFullScreenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isLandscape(getApplicationContext())) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mFullScreenIcon.setImageResource(R.drawable.fullscreen_white_18dp);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mFullScreenIcon.setImageResource(R.drawable.fullscreen_exit_white_18dp);
                }
            }
        });

        exoSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionLayout();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(videoPlayer != null && videoPlayer.isPlaying()){
            videoPlayer.pauseVideo();
        }
    }

    private Runnable decor_view_settings = new Runnable() {
        @Override
        public void run() {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            new Handler().post(decor_view_settings);
        }
    }

    private void hideSystemUI(){
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
//
//    private void showSystemUI() {
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoPlayer != null) {
            videoPlayer.releasePlayer();
        }
    }

    AlertDialog dialog;
    private void showQualityDialog(){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.quality_layout, null);
            ListView optionList = promptsView.findViewById(R.id.optionList);
            OptionListAdapter optionListAdapter = new OptionListAdapter();
            optionList.setAdapter(optionListAdapter);
            optionList.setOnItemClickListener(optionListAdapter);
            builder.setView(promptsView);
            dialog = builder.create();
            if (!Utils.isActivityDestroyed(this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }


    AlertDialog speedDialog;
    private void showSpeedDialog(){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.quality_layout, null);
            ListView optionList = promptsView.findViewById(R.id.optionList);
            SppedListAdapter sppedListAdapter = new SppedListAdapter();
            optionList.setAdapter(sppedListAdapter);
            optionList.setOnItemClickListener(sppedListAdapter);
            builder.setView(promptsView);
            speedDialog = builder.create();
            if (!Utils.isActivityDestroyed(this))
                speedDialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }

    private void setImage(){
        Glide.with(this)
                .load(thumbPath)
                .override(videoWidth, videoHeight)
                .thumbnail(.10f)
                .into((ImageView)findViewById(R.id.image));
    }

    private void setVideoLayout(){
        int position = selectedQuality;
        if(position == -1){
            position++;
        }
        metrics = Utils.getMetrics(this);
        if(metrics == null){
            return;
        }
        if (Utils.isLandscape(this)) {
            mFullScreenIcon.setImageResource(R.drawable.fullscreen_exit_white_18dp);
            videoWidth = metrics.widthPixels;
            videoHeight = metrics.heightPixels;
            hideSystemUI();
        } else {
            mFullScreenIcon.setImageResource(R.drawable.fullscreen_white_18dp);
            videoWidth = metrics.widthPixels;
            int height = (int)qualityMaps.get(qualityList.get(position)).get("height");
            int width = (int)qualityMaps.get(qualityList.get(position)).get("width");
            videoHeight = (height * metrics.widthPixels)/width;
        }
        findViewById(R.id.image).getLayoutParams().height = findViewById(R.id.nativePlayer).getLayoutParams().height = videoHeight;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setVideoLayout();
    }

    class OptionListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        @Override
        public int getCount() {
            return qualityList.size();
        }

        @Override
        public Integer getItem(int position) {
            return qualityList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return qualityList.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.quality_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText(getItem(position)+"P");
            holder.radioButton.setOnCheckedChangeListener(null);
            if(selectedQuality == position){
                holder.radioButton.setChecked(true);
            }else{
                holder.radioButton.setChecked(false);
            }
            holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        setQuality(position);
                        dialog.dismiss();
                        hideOptionLayout();
                    }
                }
            });
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setQuality(position);
            dialog.dismiss();
            hideOptionLayout();
        }

        class ViewHolder{
            TextView title;
            MaterialRadioButton radioButton;
            public ViewHolder(View view){
                title = view.findViewById(R.id.title);
                radioButton = view.findViewById(R.id.radioButton);
            }
        }
    }
    class SppedListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        @Override
        public int getCount() {
            return speedOptions.size();
        }

        @Override
        public String getItem(int position) {
            return speedOptions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return speedOptions.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.quality_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText(getItem(position));
            holder.radioButton.setOnCheckedChangeListener(null);
            if(selectedSpeed == position){
                holder.radioButton.setChecked(true);
            }else{
                holder.radioButton.setChecked(false);
            }
            holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        setPlayBackSpeed(position);
                        speedDialog.dismiss();
                        hideOptionLayout();
                    }
                }
            });
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setPlayBackSpeed(position);
            speedDialog.dismiss();
            hideOptionLayout();
        }

        class ViewHolder{
            TextView title;
            MaterialRadioButton radioButton;
            public ViewHolder(View view){
                title = view.findViewById(R.id.title);
                radioButton = view.findViewById(R.id.radioButton);
            }
        }
    }

    private void setQuality(int position){
        if(selectedQuality == position){
            return;
        }
        selectedQuality = position;
        videoUrl = (String)qualityMaps.get(qualityList.get(selectedQuality)).get("url");
        if(videoPlayer != null) {
            currentTime = videoPlayer.getCurrentPosition();
            videoPlayer.releasePlayer();
        }
        playNativePlayer();
    }
    private void setPlayBackSpeed(int position){
        if(selectedSpeed == position){
            return;
        }
        selectedSpeed = position;
        if(videoPlayer != null) {
            videoPlayer.setPlaybackSpeed(speedValues[position]);
        }
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.optionLayout).getVisibility() == View.VISIBLE){
            hideOptionLayout();
            return;
        }
        if(Utils.isLollipop()){
            if(videoPlayer != null){
                videoPlayer.pauseVideo();
            }
            findViewById(R.id.image).setVisibility(View.VISIBLE);
        }
        super.onBackPressed();
    }

    private void showOptionLayout(){
        if(findViewById(R.id.videoOptionLayout).getVisibility() == View.VISIBLE){
            return;
        }
        findViewById(R.id.optionLayout).setVisibility(View.VISIBLE);
        Animation animationBottomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_in_200ms);
        animationBottomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                findViewById(R.id.videoOptionLayout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.optionShadow).setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.optionLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideOptionLayout();
            }
        });

        findViewById(R.id.videoOptionLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        findViewById(R.id.videoOptionLayout).startAnimation(animationBottomIn);
        findViewById(R.id.quality).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQualityDialog();
            }
        });

        findViewById(R.id.speed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSpeedDialog();
            }
        });
    }

    private void hideOptionLayout(){
        if(findViewById(R.id.optionLayout).getVisibility() == View.GONE){
            return;
        }
        Animation animationBottomout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_out_200ms);
        animationBottomout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                findViewById(R.id.optionShadow).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.videoOptionLayout).setVisibility(View.GONE);
                findViewById(R.id.optionLayout).setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.videoOptionLayout).startAnimation(animationBottomout);
    }
}
