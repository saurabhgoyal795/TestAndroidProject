package com.averda.online.downloads;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.utils.Utils;

import java.io.File;

public class MyDownloadActivity extends ZTAppCompatActivity {
    private DownloadAdapter downloadAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_downloads);
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        onBackPressed();
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(MyDownloadActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(MyDownloadActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(MyDownloadActivity.this);
                        break;
                }
                return false;
            }
        });
        getDownloadFiles();
    }

    public void getDownloadFiles(){
        String basePath = getFilesDir() + "/SubjectPdf/";
        File file = new File(basePath);
        if(file.exists()){
            File[] files = file.listFiles();
            if(files == null || files.length == 0){
                findViewById(R.id.noDownloads).setVisibility(View.VISIBLE);
                findViewById(R.id.downloadList).setVisibility(View.GONE);
            }else{
                if(downloadAdapter == null){
                    downloadAdapter = new DownloadAdapter(this, files);
                    RecyclerView downloadList = findViewById(R.id.downloadList);
                    downloadList.setLayoutManager(new LinearLayoutManager(this));
                    downloadList.setAdapter(downloadAdapter);
                }else{
                    downloadAdapter.refreshValues(files);
                }
            }
        }else{
            findViewById(R.id.noDownloads).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }
}
