package com.ztn.web.utils;

import android.util.Log;

import com.ztn.web.AppConfig;


public class LogUtil {
    private static final Boolean IS_LOG = AppConfig.IS_LOG;

    public static void e(String tag, String msg) {
        if (!IS_LOG) {
            return;
        }
        Log.e(tag, msg);
    }
    public static void i(String tag, String msg) {
        if (!IS_LOG) {
            return;
        }
        Log.i(tag, msg);
    }
    public static void w(String tag, String msg) {
        if (!IS_LOG) {
            return;
        }
        Log.w(tag, msg);
    }
    public static void d(String tag, String msg) {
        if (!IS_LOG) {
            return;
        }
        Log.d(tag, msg);
    }
    public static void v(String tag, String msg) {
        if (!IS_LOG) {
            return;
        }
        Log.v(tag, msg);
    }
}
