package com.ztn.web.utils;

import android.app.Service;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DisplayUtil {

    public static DisplayMetrics getInfo(Context context){
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(metrics);
        }
        return metrics;
    }
}
