package com.ztn.web.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ztn.web.App;
import com.ztn.web.bean.response.DeviceConnectResponse;
import java.util.List;


public class MySharedPreferences {


    public static SharedPreferences share(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("date", Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public static String get(Context context, String key) {
        return share(context).getString(key, null);
    }

    public static boolean set(Context context, String key, String value) {
        SharedPreferences.Editor e = share(context).edit();
        e.putString(key, value);
        return e.commit();
    }

    public static List<DeviceConnectResponse.Activity> getLocalActivity(Context context) {
        Gson gson = App.getInstance().getGson();
        return gson.fromJson(get(context, "ACTIVITY_LIST"), new TypeToken<List<DeviceConnectResponse.Activity>>() {
        }.getType());
    }

    public static Boolean setLocalActivityList(Context context, List<DeviceConnectResponse.Activity> activityList) {
        Gson gson = App.getInstance().getGson();
        set(context, "ACTIVITY_LIST", gson.toJson(activityList));
        return true;
    }

}
