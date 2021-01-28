package com.averda.online;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.averda.online.common.ZTAppCompatActivity;
import com.averda.online.home.MainActivity;
import com.averda.online.utils.Utils;

public class SplashActivity extends ZTAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        BaseApplication.isShownAlert = false;
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                launchLoginScreen();
            }
        }, 5000);
    }

    private void launchLoginScreen(){
        Intent intent = new Intent(this, Utils.isLoginCompleted(this) ? MainActivity.class : InitialScreenActivity.class);
        startActivity(intent);
        finish();
    }
}
