package com.ztn.web.utils;

import com.ztn.web.AppConfig;
import okhttp3.*;

public class HttpUtil {

    public static final String CONNECT_CENTER_URL = AppConfig.CENTER_SERVER_BASE_URL +"app/device/connect";
    public static final String DOWNLOAD_ACTIVITY = AppConfig.CENTER_SERVER_BASE_URL +"app/activity/version/download";

    private static final String TAG = HttpUtil.class.getSimpleName();
    private static final OkHttpClient okHttpClient = new OkHttpClient();


    public static void doGet(String url, Callback callback) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url( url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public static void doPost(String url, String json, Callback callback) {
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);

        Request request = new Request.Builder()
                .url( url)//请求的url
                .post(requestBody)
                .build();

        //创建/Call
        Call call = okHttpClient.newCall(request);
        //加入队列 异步操作
        call.enqueue(callback);

    }


}
