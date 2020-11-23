package com.ztn.web;

import android.app.Application;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.google.gson.Gson;
import com.liulishuo.filedownloader.FileDownloader;


public class App extends Application {

    private final static String TAG = App.class.getSimpleName();
    private static App mInstance;




    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        FileDownloader.init(this.getApplicationContext());
        if (mInstance == null) {
            mInstance = this;
        }
//        initMqttClient();
    }



    public static App getInstance() {
        return mInstance;
    }


}
