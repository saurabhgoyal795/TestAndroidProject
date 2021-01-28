package com.averda.online;

import android.app.Application;


/**
 * Created by Rahul Hooda on 14/7/17.
 */
public class BaseApplication extends Application {
    public static boolean isShownAlert = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }


}
