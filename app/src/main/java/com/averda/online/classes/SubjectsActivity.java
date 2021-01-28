package com.averda.online.classes;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

public class SubjectsActivity extends ZTAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);

        Bundle bundle= getIntent().getExtras();
        JSONArray jsonArray = new JSONArray();
        try {
            setTitle(bundle.getString("Title"));
            jsonArray = new JSONArray(bundle.getString("topicList"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SubjectsFragment homeFragment = new SubjectsFragment(jsonArray);
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
                        Utils.openMyPackages(SubjectsActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(SubjectsActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(SubjectsActivity.this);
                        break;
                }
                return false;
            }
        });
    }
}