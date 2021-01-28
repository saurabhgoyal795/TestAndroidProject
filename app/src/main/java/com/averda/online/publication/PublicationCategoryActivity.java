package com.averda.online.publication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.view.MenuItem;

import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.utils.Utils;

import org.json.JSONObject;

public class PublicationCategoryActivity extends ZTAppCompatActivity {
    private JSONObject itemObj = new JSONObject();
    PublicationCategoryFragment homeFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publication);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String item = bundle.getString("item");
            try {
                itemObj = new JSONObject(item);
                setTitle(itemObj.optString("categoryName"));
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
        homeFragment = new PublicationCategoryFragment(itemObj);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commitAllowingStateLoss();
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        Utils.openHome(PublicationCategoryActivity.this);
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackagesNewTask(PublicationCategoryActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackagesNewTask(PublicationCategoryActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openMyProfileNewTask(PublicationCategoryActivity.this);
                        break;
                }
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        if(homeFragment != null){
            homeFragment.onActivityReenter(data);
        }
    }
}