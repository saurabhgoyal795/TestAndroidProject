package com.averda.online.mypackage.onlineClass;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.player.VimeoPlayer;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassVideoListActivity extends ZTAppCompatActivity {
    private RecyclerView videoListView;
    JSONArray videoList;
    BottomNavigationView navView;
    ClassVideoListAdapter classVideoListAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_package_class_video_list);
        videoListView = findViewById(R.id.videoList);
        try {
            Bundle bundle = getIntent().getExtras();
            videoList = new JSONArray(bundle.getString("videoList"));
            String title = bundle.getString("title");
            setTitle(title);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        if(videoList == null || videoList.length() == 0){
            findViewById(R.id.noVideos).setVisibility(View.VISIBLE);
        }else {
            setList();
            getVideoThumnails();
        }
        navView = findViewById(R.id.navigation);
        navView.getMenu().getItem(1).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        Utils.openHome(ClassVideoListActivity.this);
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(ClassVideoListActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(ClassVideoListActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(ClassVideoListActivity.this);
                        break;
                }
                return false;
            }
        });
    }

    private void setList(){
        if(classVideoListAdapter == null) {
            videoListView.setLayoutManager(new GridLayoutManager(this, 2));
            classVideoListAdapter = new ClassVideoListAdapter(this, videoList);
            videoListView.setAdapter(classVideoListAdapter);
        }else{
            classVideoListAdapter.refreshValues(videoList);
        }
    }

    AtomicInteger counter = new AtomicInteger(0);
    private void getVideoThumnails(){
        for (int i = 0 ; i < videoList.length() ; i++){
            JSONObject object = videoList.optJSONObject(i);
            String videoUrl = object.optString("VideoURL");
            String imagePath = VimeoPlayer.getVideoImagePath(this, videoUrl);
            if(Utils.isValidString(imagePath)){
                counter.incrementAndGet();
                if(counter.get() >= videoList.length()){
                    setList();
                    return;
                }
            }else{
                VimeoPlayer.getVideo(this, videoUrl, new VimeoPlayer.FetchListener() {
                    @Override
                    public void videoData(HashMap<String, Object> videoItem) {
                        counter.incrementAndGet();
                        if(counter.get() >= videoList.length()){
                            setList();
                            return;
                        }
                    }

                    @Override
                    public void error(String error) {

                    }
                });
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(videoList != null && videoList.length() > 0) {
            setList();
        }
    }
}
