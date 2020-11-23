package com.ztn.web;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import com.google.gson.reflect.TypeToken;
import com.tencent.smtt.export.external.extension.proxy.ProxyWebChromeClientExtension;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebViewClient;
import com.ztn.web.bean.call.IDownloadCallBack;
import com.ztn.web.bean.call.IJavaScriptFunction;
import com.ztn.web.bean.call.IUnzipCallBack;
import com.ztn.web.bean.requeset.DeviceConnectRequest;
import com.ztn.web.bean.response.BaseResponse;
import com.ztn.web.bean.response.DeviceConnectResponse;
import com.ztn.web.javascript_interface.BleMethod;
import com.ztn.web.javascript_interface.CommonMethod;
import com.ztn.web.javascript_interface.VolumeMethod;
import com.ztn.web.utils.DisplayUtil;
import com.ztn.web.utils.DownloadUtil;
import com.ztn.web.utils.FileUtil;
import com.ztn.web.utils.HttpUtil;
import com.ztn.web.utils.JsonUtil;
import com.ztn.web.utils.LogUtil;
import com.ztn.web.utils.NetUtils;
import com.ztn.web.utils.StorageUtil;
import com.ztn.web.utils.X5WebView;
import com.ztn.web.utils.ZipExtractorTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.Response;

public class ShowMain implements IJavaScriptFunction {

    private static final String TAG = ShowMain.class.getSimpleName();
    private final int SYSTEM_VERSION = Build.VERSION.SDK_INT;
    private String ip = null;
    private DownloadUtil downloadUtil = new DownloadUtil();
    //    private DeviceConnectResponse.Activity nowActivity = null;
    private DeviceConnectResponse.Device device = new DeviceConnectResponse.Device();
    private Handler timeHandler;
    private Map<Integer, Integer> downloadMap = new HashMap<>();
    public X5WebView webView;
    public Integer connectCenterDelay = 1;

    public AppCompatActivity appCompatActivity;

    public ShowMain(AppCompatActivity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
    }


    /**
     * 初始化环境
     */
    public void initEnv() {
        ip = NetUtils.getIPAddress(appCompatActivity);
        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                LogUtil.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        QbSdk.initX5Environment(appCompatActivity.getApplicationContext(), cb);
        webView.getSettings().setAllowContentAccess(true);
        webView.getView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSavePassword(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {

            }

            @Override
            public void onHideCustomView() {

            }
        });
        webView.setWebChromeClientExtension(new ProxyWebChromeClientExtension() {
            /*
             * callback：处理后的回调；
             * schemePlusHost：域名；
             * username：用户名；
             * password：密码；
             * nameElement：用户名输入框名称；
             * passwordElement：密码输入框名称；
             * isReplace：是否是替换操作
             */
            @Override
            public boolean onSavePassword(
                    android.webkit.ValueCallback<String> callback,
                    String schemePlusHost,
                    String username,
                    String password,
                    String nameElement,
                    String passwordElement,
                    boolean isReplace) {
                //这里可以弹窗提示用户
                //这里调用将会保存用户名和密码，如果只保存用户名可以将密码置为null，如果两者均不存在则不需要调用该接口
                webView.getX5WebViewExtension()
                        .sendRememberMsg(schemePlusHost, username, password, nameElement, passwordElement);

                //处理完后需要回调该接口，执行了保存操作参数为true，否则为false
                callback.onReceiveValue("true");
                //这里要返回true，否则内核会提示用户保存密码
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }

            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();// 接受所有网站的证书
            }
            /*android 低版本 Desperate*/

            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                LogUtil.e(TAG + " console ", message + "(" + sourceID + ":" + lineNumber + ")");
            }

            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                LogUtil.e(TAG + " console ", "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                return true;
            }

        });
        webView.addJavascriptInterface(new VolumeMethod(appCompatActivity), "volume");
        webView.addJavascriptInterface(new BleMethod(appCompatActivity, this), "ble");
        webView.addJavascriptInterface(new CommonMethod(this), "common");

        showActivity();

        timeHandler = new Handler();
        timeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connect2Center();
                timeHandler.postDelayed(this, connectCenterDelay);
            }
        }, connectCenterDelay);
        connectCenterDelay = 1000;

    }

    /**
     * 获取应用版本
     *
     * @return
     */
    private String getVersionName() {
        PackageManager packageManager = appCompatActivity.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(appCompatActivity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        String version = packInfo.versionName;
        return version;
    }

    /**
     * 连接服务器
     */
    private void connect2Center() {
        DeviceConnectRequest request = new DeviceConnectRequest();
        List<DeviceConnectResponse.Activity> localActivityList = StorageUtil.getLocalActivity();
        DisplayMetrics metrics = DisplayUtil.getInfo(appCompatActivity);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float xdpi = metrics.xdpi;
        final float ydpi = metrics.ydpi;
        request.setWidthPixels(widthPixels);
        request.setHeightPixels(heightPixels);
        request.setXdpi(xdpi);
        request.setYdpi(ydpi);
        request.setAppKey(AppConfig.APP_KEY);
        request.setAppSecret(AppConfig.APP_SECRET);
        request.setDeviceCore(AppConfig.APP_KEY + '_' + Settings.Secure.getString(appCompatActivity.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
        request.setIp(ip);
        request.setAppVersion(getVersionName());
        request.setActivityList(localActivityList);
        String reqJson = JsonUtil.toJson(request);
//        MyLog.i(TAG,reqJson);
        HttpUtil.doPost(HttpUtil.CONNECT_CENTER_URL, reqJson, new Callback() {

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                LogUtil.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    LogUtil.e(TAG, "response is not success");
                    return;
                }
                String res = response.body().string();
                LogUtil.i(TAG, "response:" + res);
                BaseResponse<DeviceConnectResponse> baseResponse = JsonUtil.fromJson(res, new TypeToken<BaseResponse<DeviceConnectResponse>>() {
                }.getType());
                if (BaseResponse.SUCCESS_CODE.equals(baseResponse.getCode())) {
                    List<DeviceConnectResponse.Activity> addActivityList = baseResponse.getData().getAddActivityList();
                    List<DeviceConnectResponse.Activity> delActivityList = baseResponse.getData().getDeleteActivityList();
                    List<DeviceConnectResponse.Activity> openActivityList = baseResponse.getData().getOpenActivityList();
                    List<DeviceConnectResponse.Activity> closeActivityList = baseResponse.getData().getCloseActivityList();
                    List<DeviceConnectResponse.Activity> updateActivityList = baseResponse.getData().getUpdateActivityList();
                    device = baseResponse.getData().getDevice();
                    compare(localActivityList, addActivityList, delActivityList, openActivityList, closeActivityList, updateActivityList);
                }
            }
        });
    }

    /**
     * 增加或删除活动
     *
     * @param addActivityList
     * @param delActivityList
     */
    private void compare(List<DeviceConnectResponse.Activity> localActivityList, List<DeviceConnectResponse.Activity> addActivityList, List<DeviceConnectResponse.Activity> delActivityList, List<DeviceConnectResponse.Activity> openActivityList, List<DeviceConnectResponse.Activity> closeActivityList, List<DeviceConnectResponse.Activity> updateActivityList) {
        if (null != openActivityList) {
            for (DeviceConnectResponse.Activity openActivity : openActivityList) {
                for (Integer i = 0; i < localActivityList.size(); i++) {
                    DeviceConnectResponse.Activity localActivity = localActivityList.get(i);
                    if (localActivity.getActivityVersionId().equals(openActivity.getActivityVersionId())) {
                        localActivityList.set(i, openActivity);
                        StorageUtil.setLocalActivityList(localActivityList);
                        showActivity();
                        continue;
                    }
                }
            }
        }
        if (null != closeActivityList) {
            for (DeviceConnectResponse.Activity closeActivity : closeActivityList) {
                for (Integer i = 0; i < localActivityList.size(); i++) {
                    DeviceConnectResponse.Activity localActivity = localActivityList.get(i);
                    if (localActivity.getActivityVersionId().equals(closeActivity.getActivityVersionId())) {
                        localActivityList.set(i, closeActivity);
                        StorageUtil.setLocalActivityList(localActivityList);
                        showActivity();
                        continue;
                    }
                }
            }
        }
        if (null != delActivityList) {
            for (DeviceConnectResponse.Activity delActivity : delActivityList) {
                FileUtil.delFile(new File(AppConfig.ACTIVITY_PATH + AppConfig.APP_KEY + File.separator + delActivity.getActivityKey() + File.separator + delActivity.getActivityVersionId()));
                Iterator<DeviceConnectResponse.Activity> iterator = localActivityList.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getActivityVersionId().equals(delActivity.getActivityVersionId())) {
                        iterator.remove();
                        break;
                    }
                }
                StorageUtil.setLocalActivityList(localActivityList);
            }
        }
        if (null != addActivityList) {
            for (DeviceConnectResponse.Activity activity : addActivityList) {
                downloadActivity(activity);
            }
        }
        if (null != updateActivityList) {
            for (DeviceConnectResponse.Activity activity : updateActivityList) {
                downloadActivity(activity);
            }
        }
    }


    private void downloadActivity(DeviceConnectResponse.Activity activity) {
        if (null != downloadMap.get(activity.getActivityVersionId())) {
            return;
        } else {
            LogUtil.i(TAG, "当前下载列表" + JsonUtil.toJson(downloadMap));
        }
        FileUtil.delFile(new File(AppConfig.ACTIVITY_PATH + AppConfig.APP_KEY + File.separator + activity.getActivityKey() + File.separator + activity.getActivityVersionId()));
        String url = activity.getPath();
        LogUtil.i(TAG, "下载活动文件-" + activity.getActivityVersionId());
        Integer downloadTaskId = downloadUtil.createTask(url, AppConfig.ACTIVITY_PATH + AppConfig.APP_KEY + File.separator + activity.getActivityKey() + File.separator + activity.getActivityVersionId() + File.separator + "app.zip", new IDownloadCallBack() {

            @Override
            public void progress(Integer taskId, Integer soFarBytes, Integer totalBytes) {
                LogUtil.i(TAG, "活动-" + activity.getActivityVersionId() + "-----progress---------" + soFarBytes);
            }

            @Override
            public void error(Integer taskId, String msg) {
                LogUtil.i(TAG, "活动-" + activity.getActivityVersionId() + "文件下载失败" + msg);
                downloadMap.remove(activity.getActivityVersionId());
            }

            @Override
            public void completed(Integer taskId) {
                LogUtil.i(TAG, "活动-" + activity.getActivityVersionId() + "文件下载完成");
                unzipActivity(activity);
            }
        });
        downloadMap.put(activity.getActivityVersionId(), downloadTaskId);
    }


    private Boolean unzipActivity(DeviceConnectResponse.Activity activity) {
        List<DeviceConnectResponse.Activity> localActivityList = StorageUtil.getLocalActivity();
        LogUtil.i(TAG, "解压活动-" + activity.getActivityVersionId() + "文件");
        String zipPath = AppConfig.ACTIVITY_PATH + AppConfig.APP_KEY + File.separator + activity.getActivityKey() + File.separator + activity.getActivityVersionId() + File.separator;
        try {
            ZipExtractorTask task = new ZipExtractorTask(zipPath + "app.zip", zipPath, true, new IUnzipCallBack() {
                @Override
                public Long doInBackground(Void... params) {
                    return null;
                }

                @Override
                public void onPostExecute(Long result) {
                    downloadMap.remove(activity.getActivityKey());
                    Iterator<DeviceConnectResponse.Activity> iterator = localActivityList.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().getActivityKey().equals(activity.getActivityKey())) {
                            iterator.remove();
                        }
                    }
                    localActivityList.add(activity);
                    StorageUtil.setLocalActivityList(localActivityList);
                    showActivity();
//                    appCompatActivity.runOnUiThread(() -> {
//                        MyAlertDialog myAlertDialog = new MyAlertDialog(appCompatActivity).builder()
//                                .setTitle("更新成功")
//                                .setMsg("欢迎使用")
//                                .setPositiveButton("确认", v -> {
//
//                                }).setNegativeButton("取消", v -> {
//
//                                });
//                        myAlertDialog.show();
//                    });
                }

                @Override
                public void onPreExecute() {

                }

                @Override
                public void onProgressUpdate(Integer... values) {
                }
            });
            task.execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 展示活动
     */
    synchronized void showActivity() {
        DeviceConnectResponse.Activity nowActivity = StorageUtil.getNowActivity();
        loadUrl(nowActivity);
        List<DeviceConnectResponse.Activity> activityList = StorageUtil.getLocalActivity();
        if (null == activityList || 0 == activityList.size()) {
            return;
        }

        LogUtil.e(TAG, JsonUtil.toJson(activityList));
        DeviceConnectResponse.Activity tryActivity = null;
        for (Integer i = 0; i < activityList.size(); i++) {
            DeviceConnectResponse.Activity activity = activityList.get(i);
            if (!activity.getDisable()) {
                tryActivity = activity;
                break;
            }
        }

        LogUtil.e(TAG, null != nowActivity ? "" + nowActivity.getActivityVersionId() : "nowActivity is null");
        LogUtil.e(TAG, null != tryActivity ? "" + tryActivity.getActivityVersionId() : "tryActivity is null");
        if (null != nowActivity && null != tryActivity && tryActivity.getActivityVersionId().equals(nowActivity.getActivityVersionId())) {
            return;
        }
        loadUrl(tryActivity);
    }

    public String makeUrl(DeviceConnectResponse.Activity activity) {
        if (null == activity) {
            return AppConfig.DEFAULT_APP_PATH;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("file:///");
        builder.append(AppConfig.ACTIVITY_PATH);
        builder.append(AppConfig.APP_KEY);
        builder.append(File.separator);
        builder.append(activity.getActivityKey());
        builder.append(File.separator);
        builder.append(activity.getActivityVersionId());
        builder.append(File.separator);
        builder.append(activity.getIndexFile());
        builder.append("?companyId=" + AppConfig.COMPANY_ID);
        return builder.toString();
    }

    /**
     * 启动活动
     *
     * @param url
     */
    private void loadUrl(DeviceConnectResponse.Activity activity) {
        List<DeviceConnectResponse.Activity> activityList = StorageUtil.getLocalActivity();
        if (null != activity) {
            Boolean checkResult = checkActivityRunFile(activityList, activity);
            if (!checkResult) {
                downloadActivity(activity);
                return;
            }
        }
        String url = makeUrl(activity);
        appCompatActivity.runOnUiThread(() -> {
            if (url.equals(webView.getUrl())) {
                LogUtil.i(TAG, "重复播放改url");
                return;
            }
            if (url.equals(AppConfig.DEFAULT_APP_PATH)) {
                connectCenterDelay = 1000;
                LogUtil.i(TAG, "启动默认活动");
            } else {
                connectCenterDelay = 1000 * 5;
                LogUtil.i(TAG, "当前url：" + url);
            }
            LogUtil.i(TAG, "重绘");
            webView.loadUrl(url);
            webView.postDelayed(() -> webView.clearHistory(), 1000);
            StorageUtil.setNowActivity(activity);
        });
    }

    /**
     * 检查活动文件是否存在
     *
     * @param appKey
     * @param activityList
     * @param activity
     * @return
     */
    private Boolean checkActivityRunFile(List<DeviceConnectResponse.Activity> activityList, DeviceConnectResponse.Activity activity) {
        if (new File(AppConfig.ACTIVITY_PATH + AppConfig.APP_KEY + File.separator + activity.getActivityKey() + File.separator + activity.getActivityVersionId() + File.separator + activity.getIndexFile()).exists()) {
            return true;
        } else {
            LogUtil.i(TAG, "活动" + activity.getActivityVersionId() + "执行文件失踪了");
            Iterator<DeviceConnectResponse.Activity> iterator = activityList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getActivityKey().equals(activity.getActivityKey())) {
                    iterator.remove();
                    break;
                }
            }
            StorageUtil.setLocalActivityList(activityList);
            return false;
        }
    }


    /**
     * 获取系统消息
     */
    private void printSystemInfo() {
        final String boardName = android.os.Build.BOARD;//获取设备基板名称(sdm710)
        final String bootloader = android.os.Build.BOOTLOADER;//获取设备引导程序版本号(unknown)
        final String brand = android.os.Build.BRAND;//获取设备品牌(OPPO)
        final String cpuAbi = android.os.Build.CPU_ABI;//获取设备指令集名称（CPU的类型）(arm64-v8a)
        final String device = android.os.Build.DEVICE;//获取设备驱动名称(OP4679)
        final String display = android.os.Build.DISPLAY;//获取设备显示的版本包（在系统设置中显示为版本号）和ID一样(display-PCGM00_11_A.08)
        final String fingerprint = android.os.Build.FINGERPRINT;//设备的唯一标识。由设备的多个信息拼接合成。(OPPO/PCGM00/OP4679:9/PKQ1.190101.001/1568962300:user/release-keys)
        final String hardWare = android.os.Build.HARDWARE;//设备硬件名称,一般和基板名称一样（BOARD）(qcom)
        final String host = android.os.Build.HOST;//设备主机地址(CP-ubuntu-123)
        final String id = android.os.Build.ID;//设备版本号。(PKQ1.190101.001)
        final String model = android.os.Build.MODEL;//获取手机的型号 设备名称。(PCGM00)
        final String manufactuere = android.os.Build.MANUFACTURER;//获取设备制造商(OPPO)
        final String product = android.os.Build.PRODUCT;//整个产品的名称(PCGM00)
        final String tags = android.os.Build.TAGS;//设备标签。如release-keys 或测试的 test-keys(release-keys)
        final Long time = android.os.Build.TIME;//时间(1569600822000)
        final String type = android.os.Build.TYPE;//设备版本类型  主要为"user" 或"eng".(user)
        final String user = android.os.Build.USER;//设备用户名 基本上都为android-build(root)
        final String versionRelease = android.os.Build.VERSION.RELEASE;//获取系统版本字符串。如4.1.2 或2.2 或2.3等(9)
        final String versionCodename = android.os.Build.VERSION.CODENAME;//设备当前的系统开发代号，一般使用REL代替(REL)
        final String versionIncremental = android.os.Build.VERSION.INCREMENTAL;//系统源代码控制值，一个数字或者git hash值(1569600822)
        final String sdk = android.os.Build.VERSION.SDK;//系统的API级别 一般使用下面大的SDK_INT 来查看(28)
        final Integer sdkInt = android.os.Build.VERSION.SDK_INT;//系统的API级别 数字表示(28)
        LogUtil.i(TAG, "boardName-" + boardName);
        LogUtil.i(TAG, "bootloader-" + bootloader);
        LogUtil.i(TAG, "brand-" + brand);
        LogUtil.i(TAG, "cpuAbi-" + cpuAbi);
        LogUtil.i(TAG, "device-" + device);
        LogUtil.i(TAG, "display-" + display);
        LogUtil.i(TAG, "fingerprint-" + fingerprint);
        LogUtil.i(TAG, "hardWare-" + hardWare);
        LogUtil.i(TAG, "host-" + host);
        LogUtil.i(TAG, "id-" + id);
        LogUtil.i(TAG, "model-" + model);
        LogUtil.i(TAG, "manufactuere-" + manufactuere);
        LogUtil.i(TAG, "product-" + product);
        LogUtil.i(TAG, "tags-" + tags);
        LogUtil.i(TAG, "time-" + time);
        LogUtil.i(TAG, "type-" + type);
        LogUtil.i(TAG, "user-" + user);
        LogUtil.i(TAG, "versionRelease-" + versionRelease);
        LogUtil.i(TAG, "versionCodename-" + versionCodename);
        LogUtil.i(TAG, "versionIncremental-" + versionIncremental);
        LogUtil.i(TAG, "sdk-" + sdk);
        LogUtil.i(TAG, "sdkInt-" + sdkInt);
    }

    @Override
    public void evak(String function) {
        String func = "javascript:" + function;
        LogUtil.i(TAG, "evak " + func);
        appCompatActivity.runOnUiThread(() -> {
            if (SYSTEM_VERSION < 18) {
                webView.loadUrl(func);
            } else {
                webView.evaluateJavascript(func, value -> {

                });
            }
        });
    }


}
