package com.ztn.web.utils;

import com.google.gson.Gson;
import com.ztn.web.App;
import com.ztn.web.AppConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtils {

    public static final String CONNECT_CENTER_URL = AppConfig.CENTER_SERVER_BASE_URL +"device/connect";
    public static final String DOWNLOAD_RUNFILE = AppConfig.CENTER_SERVER_BASE_URL +"activity/download/runFile";

    private static final String TAG = HttpUtils.class.getSimpleName();
    private static final OkHttpClient okHttpClient = new OkHttpClient();


    public static void doGet(String url, Callback callback) {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url( url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public static void doPost(String url, Object object, Callback callback) {
        Gson gson = App.getInstance().getGson();
        String json = gson.toJson(object);
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
