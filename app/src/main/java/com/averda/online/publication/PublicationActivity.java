package com.averda.online.publication;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;

import android.view.MenuItem;

import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.utils.Utils;

public class PublicationActivity extends ZTAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publication);
        PublicationFragment homeFragment = new PublicationFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commitAllowingStateLoss();
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        onBackPressed();
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(PublicationActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(PublicationActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(PublicationActivity.this);
                        break;
                }
                return false;
            }
        });
    }
}