package com.ztn.web.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ztn.web.App;
import com.ztn.web.bean.response.DeviceConnectResponse;

import java.util.ArrayList;
import java.util.List;


public class StorageUtil {

    public static final Context context = App.getInstance();


    public static SharedPreferences share() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("date", Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public static String get(String key) {
        return share().getString(key, null);
    }

    public static boolean set(String key, String value) {
        SharedPreferences.Editor e = share().edit();
        e.putString(key, value);
        return e.commit();
    }

    public static List<DeviceConnectResponse.Activity> getLocalActivity() {
        List<DeviceConnectResponse.Activity> list = JsonUtil.fromJson(get("ACTIVITY_LIST"), new TypeToken<List<DeviceConnectResponse.Activity>>() {
        }.getType());
        if (null == list) {
            return new ArrayList<>();
        }
        return list;
    }

    public static Boolean setLocalActivityList(List<DeviceConnectResponse.Activity> activityList) {
        set("ACTIVITY_LIST", JsonUtil.toJson(activityList));
        return true;
    }

    public static DeviceConnectResponse.Activity getNowActivity(){
        DeviceConnectResponse.Activity activity = JsonUtil.fromJson(get("ACTIVITY_NOW"), DeviceConnectResponse.Activity.class);
        return activity;
    }

    public static Boolean setNowActivity(DeviceConnectResponse.Activity activity){
        set("ACTIVITY_NOW", JsonUtil.toJson(activity));
        return true;
    }

}
