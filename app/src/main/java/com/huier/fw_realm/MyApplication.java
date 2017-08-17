package com.huier.fw_realm;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.huier.fw_realm.model.Parent;

import io.realm.Realm;
import io.realm.RealmConfiguration;

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

//        RealmConfiguration configuration = new RealmConfiguration.Builder()
//                .deleteRealmIfMigrationNeeded()
//                .build();
//        Realm.setDefaultConfiguration(configuration);

        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.createObject(Parent.class);
                    }})
                .build();
        Realm.deleteRealm(realmConfig); // Delete Realm between app restarts.
        Realm.setDefaultConfiguration(realmConfig);


        //Stetho初始化
        Stetho.initializeWithDefaults(this);
    }
}
