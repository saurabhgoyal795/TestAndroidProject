package com.averda.online.notification;

import android.os.Bundle;

import com.averda.online.R;
import com.averda.online.common.ZTAppCompatActivity;

public class NotificationActivity extends ZTAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        NotificationFragment homeFragment = new NotificationFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commitAllowingStateLoss();
    }
}