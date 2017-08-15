package com.huier.fw_realm;

import android.app.Application;

import io.realm.Realm;

/**
 * 作者：张玉辉
 * 时间：2017/8/15.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this);
    }
}
