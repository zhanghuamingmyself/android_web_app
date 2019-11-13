package com.ztn.web;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Call;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hb.dialog.myDialog.MyAlertDialog;
import com.tencent.bugly.Bugly;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebViewClient;
import com.ztn.web.bean.FileConfig;
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
import com.ztn.web.utils.DownloadUtils;
import com.ztn.web.utils.FileUtils;
import com.ztn.web.utils.HttpUtils;
import com.ztn.web.utils.MyLog;
import com.ztn.web.utils.MySharedPreferences;
import com.ztn.web.utils.X5WebView;
import com.ztn.web.utils.ZipExtractorTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements IJavaScriptFunction {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final int SYSTEM_VERSION = Build.VERSION.SDK_INT;
    private Boolean isDebug = false;
    private String ip = "0.0.0.1";
    private Gson gson;
    private FileConfig fileConfig;
    private DownloadUtils downloadUtils = new DownloadUtils();

    private Button mBtnConnect;
    private Button mBtnClean;
    private TextView mTvMessage;
    private EditText mEditText;
    private Button mBtnGoUrl;
    private Button mBtnDebug;
    private DeviceConnectResponse.Activity nowActivity = null;
    private DeviceConnectResponse.Device nowDevice = new DeviceConnectResponse.Device();
    private Handler timeHandler;
    private Boolean isShowDialog = true;
    private Map<String, Integer> downloadMap = new HashMap<>();
    X5WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        gson = App.getInstance().getGson();
        initView();
        checkPermission();

    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            initEnv();
        }
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                return;
            }
        }
        initEnv();
    }

    /**
     * 初始化环境
     */
    void initEnv() {
        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        QbSdk.initX5Environment(getApplicationContext(), cb);
        webView.getSettings().setAllowContentAccess(true);
        webView.getView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSavePassword(true);
        webView.getSettings().setSupportMultipleWindows(true);
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
                MyLog.e(TAG + " console ", message + "(" + sourceID + ":" + lineNumber + ")");
            }

            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                MyLog.e(TAG + " console ", "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                return true;
            }

        });
        webView.addJavascriptInterface(new VolumeMethod(this), "volume");
        webView.addJavascriptInterface(new BleMethod(this, this), "ble");
        webView.addJavascriptInterface(new CommonMethod(this), "common");
        loadUrl(AppConfig.DEFAULT_APP_PATH);
        File configPath = new File(AppConfig.CONFIG_PATH);
        if (!configPath.exists()) {
            FileUtils.getInstance(MainActivity.this).copyAssetsToSD("config", AppConfig.CONFIG_PATH);
        }
        fileConfig = FileUtils.readCofigFile(getContentResolver());
        if (null == fileConfig) {
            textLog("缺少配置文件");
            showDevTool();
            return;
        }
        if (null == fileConfig.getAppKey()) {
            textLog("缺少AppKey");
            showDevTool();
            return;
        }
        if (null == fileConfig.getAppSecret()) {
            textLog("缺少AppSecret");
            showDevTool();
            return;
        }
        isDebug = fileConfig.getIsDebug();
        App.getInstance().setFileConfig(fileConfig);
        textLog("应用KEY：" + fileConfig.getAppKey());
        textLog("设备码：" + fileConfig.getDeviceCore());
        Bugly.init(getApplicationContext(), "29c88f8c62", false);

        if (isDebug) {
            return;
        }
        showActivity();
        connect2Center();

//        timeHandler = new Handler();
//        timeHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (isDebug) {
//                    return;
//                }
//                showActivity();
//                connect2Center();
//                if (null != nowDevice && null != nowDevice.getConnectDelay()) {
//                    timeHandler.postDelayed(this, nowDevice.getConnectDelay());
//                } else {
//                    timeHandler.postDelayed(this, 1000 * 5);
//                }
//
//            }
//        }, 1000);//每两秒执行一次runnable.
    }

    /**
     * 获取应用版本
     *
     * @return
     */
    private String getVersionName() {
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(), 0);
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
        fileConfig = App.getInstance().getFileConfig();
        DeviceConnectRequest request = new DeviceConnectRequest();
        if (null == fileConfig) {
            return;
        }
        List<DeviceConnectResponse.Activity> localActivityList = MySharedPreferences.getLocalActivity(MainActivity.this);
        DisplayMetrics metrics = DisplayUtil.getInfo(MainActivity.this);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float xdpi = metrics.xdpi;
        final float ydpi = metrics.ydpi;
        request.setWidthPixels(widthPixels);
        request.setHeightPixels(heightPixels);
        request.setXdpi(xdpi);
        request.setYdpi(ydpi);
        request.setAppKey(fileConfig.getAppKey());
        request.setAppSecret(fileConfig.getAppSecret());
        request.setDeviceCore(fileConfig.getDeviceCore());
        request.setIp(ip);
        request.setAppVersion(getVersionName());
        request.setCleanAll(false);
        request.setActivityList(localActivityList);
        if (null != nowActivity) {
            request.setActivityKey(nowActivity.getActivityKey());
        }
        HttpUtils.doPost(HttpUtils.CONNECT_CENTER_URL, request, new Callback() {

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                MyLog.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    MyLog.e(TAG, "response is not success");
                    return;
                }
                String res = response.body().string();
                MyLog.i(TAG, "response:" + res);
                try {
                    BaseResponse<DeviceConnectResponse> baseResponse = gson.fromJson(res, new TypeToken<BaseResponse<DeviceConnectResponse>>() {
                    }.getType());
                    if (BaseResponse.SUCCESS_CODE.equals(baseResponse.getCode())) {
                        List<DeviceConnectResponse.Activity> addActivityList = baseResponse.getData().getAddActivityList();
                        List<DeviceConnectResponse.Activity> delActivityList = baseResponse.getData().getDelActivityList();
                        List<DeviceConnectResponse.Activity> openActivityList = baseResponse.getData().getOpenActivityList();
                        List<DeviceConnectResponse.Activity> closeActivityList = baseResponse.getData().getCloseActivityList();
                        List<DeviceConnectResponse.Activity> updateActivityList = baseResponse.getData().getUpdateActivityList();
                        nowDevice = baseResponse.getData().getDevice();
                        if (null != nowDevice && null != nowDevice.getCleanAll() && nowDevice.getCleanAll()) {
                            clean();
                        }
                        if (null != nowDevice && null != nowDevice.getShowDevTool() && nowDevice.getShowDevTool()) {
                            showDevTool();
                        } else {
                            hideDevTool();
                        }
                        compare(localActivityList, addActivityList, delActivityList, openActivityList, closeActivityList, updateActivityList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
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
        if (null != addActivityList) {
            for (DeviceConnectResponse.Activity addActivity : addActivityList) {
                if (null == localActivityList) {
                    localActivityList = new ArrayList<>();
                    downloadActivity(localActivityList, addActivity);
                } else {
                    downloadActivity(localActivityList, addActivity);
                }
            }
        }
        if (null != updateActivityList) {
            for (DeviceConnectResponse.Activity updateActivity : updateActivityList) {
                for (DeviceConnectResponse.Activity localActivity : localActivityList) {
                    if (localActivity.getActivityKey().equals(updateActivity.getActivityKey())) {
                        if (!localActivity.getVersion().equals(updateActivity.getVersion())) {
                            downloadActivity(localActivityList, updateActivity);
                            break;
                        }
                    }
                }
            }
        }
        if (null != openActivityList) {
            for (DeviceConnectResponse.Activity openActivity : openActivityList) {
                for (Integer i = 0; i < localActivityList.size(); i++) {
                    DeviceConnectResponse.Activity localActivity = localActivityList.get(i);
                    if (localActivity.getActivityKey().equals(openActivity.getActivityKey())) {
                        if (!localActivity.getOpen().equals(openActivity.getOpen())) {
                            localActivityList.set(i, openActivity);
                            MySharedPreferences.setLocalActivityList(MainActivity.this, localActivityList);
                        }
                        continue;
                    }
                }

            }
        }
        if (null != closeActivityList) {
            for (DeviceConnectResponse.Activity closeActivity : closeActivityList) {
                for (Integer i = 0; i < localActivityList.size(); i++) {
                    DeviceConnectResponse.Activity localActivity = localActivityList.get(i);
                    if (localActivity.getActivityKey().equals(closeActivity.getActivityKey())) {
                        if (!localActivity.getOpen().equals(closeActivity.getOpen())) {
                            localActivityList.set(i, closeActivity);
                            MySharedPreferences.setLocalActivityList(MainActivity.this, localActivityList);
                        }
                        continue;
                    }
                }
            }
        }
        if (null != delActivityList) {
            for (DeviceConnectResponse.Activity delActivity : delActivityList) {
                File dir = new File(AppConfig.ACTIVITY_PATH + App.getInstance().getFileConfig().getAppKey() + File.separator + delActivity.getActivityKey());
                FileUtils.delFile(dir);
                Iterator<DeviceConnectResponse.Activity> iterator = localActivityList.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getActivityKey().equals(delActivity.getActivityKey())) {
                        iterator.remove();
                        break;
                    }
                }
                MySharedPreferences.setLocalActivityList(MainActivity.this, localActivityList);
            }
        }
    }


    private void downloadActivity(List<DeviceConnectResponse.Activity> localList, DeviceConnectResponse.Activity activity) {
        if (null != downloadMap.get(activity.getActivityKey())) {
            return;
        } else {
            textLog("当前下载列表" + gson.toJson(downloadMap));
        }
        FileUtils.delFile(new File(AppConfig.ACTIVITY_PATH + App.getInstance().getFileConfig().getAppKey() + File.separator + activity.getActivityKey()));
        String url = HttpUtils.DOWNLOAD_RUNFILE + "?key=" + activity.getActivityKey();
        textLog("下载活动文件-" + activity.getActivityName());
        Integer downloadTaskId = downloadUtils.createTask(url, AppConfig.ACTIVITY_PATH + App.getInstance().getFileConfig().getAppKey() + File.separator + activity.getActivityKey() + File.separator + "app.zip", new IDownloadCallBack() {

            @Override
            public void progress(Integer taskId, Integer soFarBytes, Integer totalBytes) {
                MyLog.i(TAG, "活动-" + activity.getActivityName() + "-----progress---------" + soFarBytes);
            }

            @Override
            public void error(Integer taskId, String msg) {
                textLog("活动-" + activity.getActivityName() + "文件下载失败" + msg);
                downloadMap.remove(activity.getActivityKey());
            }

            @Override
            public void completed(Integer taskId) {
                textLog("活动-" + activity.getActivityName() + "文件下载完成");
                unzipActivity(localList, activity);
            }
        });
        downloadMap.put(activity.getActivityKey(), downloadTaskId);
    }


    private Boolean unzipActivity(List<DeviceConnectResponse.Activity> localList, DeviceConnectResponse.Activity activity) {

        final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
        textLog("解压活动-" + activity.getActivityName() + "文件");
        String zipPath = AppConfig.ACTIVITY_PATH + App.getInstance().getFileConfig().getAppKey() + File.separator + activity.getActivityKey() + File.separator;
        try {
            ZipExtractorTask task = new ZipExtractorTask(zipPath + "app.zip", zipPath, true, new IUnzipCallBack() {
                @Override
                public Long doInBackground(Void... params) {
                    return null;
                }

                @Override
                public void onPostExecute(Long result) {
                    localList.add(activity);
                    downloadMap.remove(activity.getActivityKey());
                    MySharedPreferences.setLocalActivityList(MainActivity.this, localList);
                    showActivity();
                    if (isShowDialog && mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                }

                @Override
                public void onPreExecute() {
                    if (isShowDialog && mDialog != null) {
                        mDialog.setTitle("解压活动中");
                        mDialog.setMessage(activity.getActivityName());
                        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mDialog.setOnCancelListener(dialog -> {
                            // TODO Auto-generated method stub

                        });
                        mDialog.show();
                    }
                }

                @Override
                public void onProgressUpdate(Integer... values) {
                    if (!isShowDialog || mDialog == null)
                        return;
                    if (values.length > 1) {
                        int max = values[1];
                        mDialog.setMax(max);
                    } else {
                        mDialog.setProgress(values[0].intValue());
                    }
                }
            });
            task.execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    void initView() {

        mTvMessage = findViewById(R.id.tv_message);
        webView = findViewById(R.id.wv_main);
        mBtnConnect = findViewById(R.id.btn_connect);
        mBtnClean = findViewById(R.id.btn_clean);
        mBtnGoUrl = findViewById(R.id.btn_goto_url);
        mEditText = findViewById(R.id.et_url);
        mBtnDebug = findViewById(R.id.btn_debug);
        mBtnConnect.setOnClickListener(view -> {
            showActivity();
            connect2Center();
        });
        mBtnClean.setOnClickListener((View view) -> {
            mTvMessage.setText("");
        });
        mBtnClean.setLongClickable(true);
        mBtnClean.setOnLongClickListener(view -> {
            clean();
            return true;
        });
        mBtnGoUrl.setOnClickListener(view -> {
            mEditText.setText("http://192.168.0.108:8080");
            isDebug = true;
            showDevTool();
            webView.loadUrl(mEditText.getText().toString());
        });
        mBtnDebug.setOnLongClickListener(view -> {
            if (isDebug) {
                hideDevTool();
            } else {
                showDevTool();
            }
            return true;
        });

    }

    /**
     * 清空全部活动
     */
    private void clean() {
        loadUrl(AppConfig.DEFAULT_APP_PATH);
        FileUtils.delFile(new File(AppConfig.ACTIVITY_PATH));
        MySharedPreferences.setLocalActivityList(MainActivity.this, null);
        nowDevice.setCleanAll(false);
        nowActivity = null;
        downloadMap = new HashMap<>();
        mTvMessage.setText("已清空全部活动");
    }


    /**
     * 可视化日志打印
     *
     * @param str
     */
    public void textLog(String str) {
        MyLog.e(TAG, "textLog+" + str);
        runOnUiThread(() -> {
            String text = mTvMessage.getText().toString();
            Integer textLineSize = text.split("\n\r").length;
            if (textLineSize > 20) {
                mTvMessage.setText("");
                text = "";
            }
            text = text + str + '\n';
            mTvMessage.setText(text);
        });
    }


    /**
     * 展示活动
     */
    void showActivity() {
        fileConfig = App.getInstance().getFileConfig();
        if (null == fileConfig) {
            return;
        }
        String appKey = fileConfig.getAppKey();
        if (null == appKey) {
            return;
        }
        List<DeviceConnectResponse.Activity> activityList = MySharedPreferences.getLocalActivity(MainActivity.this);
        if (null == activityList || 1 > activityList.size()) {
            loadUrl(AppConfig.DEFAULT_APP_PATH);
            return;
        }

        Boolean haveOpenActivity = false;
        for (Integer i = 0; i < activityList.size(); i++) {
            DeviceConnectResponse.Activity activity = activityList.get(i);
            if (activity.getOpen()) {
                haveOpenActivity = true;
                if (null != nowActivity) {
                    String nowActivityKey = nowActivity.getActivityKey();
                    String nowActivityVersion = nowActivity.getVersion();
                    if (nowActivityKey.equals(activity.getActivityKey()) && nowActivityVersion.equals(activity.getVersion())) {
                        checkActivityRunFile(appKey, activityList, activity);
                        break;
                    }
                }

                Boolean checkResult = checkActivityRunFile(appKey, activityList, activity);
                if (checkResult) {
                    nowActivity = activity;
                    showUpdateDialog(appKey, activity);
                }
                break;
            }
        }
        if (!haveOpenActivity) {
            loadUrl(AppConfig.DEFAULT_APP_PATH);
        }
    }

    private void showUpdateDialog(String appKey, DeviceConnectResponse.Activity activity) {
        if (null != activity.getIsShowDialog()) {
            isShowDialog = activity.getIsShowDialog();
        }
        if (!isShowDialog || null == nowActivity) {
            textLog("启动活动" + activity.getActivityName());
            loadUrl("file:///" + AppConfig.ACTIVITY_PATH + appKey + File.separator + activity.getActivityKey() + File.separator + "index.html");
            return;
        }
        runOnUiThread(() -> {
            MyAlertDialog myAlertDialog = new MyAlertDialog(this).builder()
                    .setTitle("更新成功")
                    .setMsg("欢迎使用")
                    .setPositiveButton("确认", v -> {
                        textLog("启动活动" + activity.getActivityName());
                        loadUrl("file:///" + AppConfig.ACTIVITY_PATH + appKey + File.separator + activity.getActivityKey() + File.separator + "index.html");
                    }).setNegativeButton("取消", v -> {

                    });
            myAlertDialog.show();
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
    private Boolean checkActivityRunFile(String appKey, List<DeviceConnectResponse.Activity> activityList, DeviceConnectResponse.Activity activity) {
        if (new File(AppConfig.ACTIVITY_PATH + appKey + File.separator + activity.getActivityKey() + File.separator + "index.html").exists()) {
            return true;
        } else {
            textLog("活动" + activity.getActivityName() + "执行文件失踪了");
            Iterator<DeviceConnectResponse.Activity> iterator = activityList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getActivityKey().equals(activity.getActivityKey())) {
                    iterator.remove();
                    break;
                }
            }
            MySharedPreferences.setLocalActivityList(MainActivity.this, activityList);
            loadUrl(AppConfig.DEFAULT_APP_PATH);
            return false;
        }
    }


    private void loadUrl(final String url) {
        if (url.equals(webView.getUrl())) {
            return;
        } else {
            textLog("当前url：" + url);
        }
        if (url.equals(AppConfig.DEFAULT_APP_PATH)) {
            nowActivity = null;
            textLog("启动默认活动");
        }
        runOnUiThread(() -> {
            webView.loadUrl(url);
            webView.postDelayed(() -> webView.clearHistory(), 1000);
        });
    }

    public void showDevTool() {
        isDebug = true;
        runOnUiThread(() -> {
            mBtnClean.setVisibility(View.VISIBLE);
            mBtnConnect.setVisibility(View.VISIBLE);
            mTvMessage.setVisibility(View.VISIBLE);
            mEditText.setVisibility(View.VISIBLE);
            mBtnGoUrl.setVisibility(View.VISIBLE);
            mBtnDebug.setVisibility(View.VISIBLE);
        });
    }

    public void hideDevTool() {
        isDebug = false;
        runOnUiThread(() -> {
            mBtnClean.setVisibility(View.INVISIBLE);
            mBtnConnect.setVisibility(View.INVISIBLE);
            mTvMessage.setVisibility(View.INVISIBLE);
            mEditText.setVisibility(View.INVISIBLE);
            mBtnGoUrl.setVisibility(View.INVISIBLE);
            mBtnDebug.setVisibility(View.INVISIBLE);
        });

    }

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
        MyLog.i(TAG, "boardName-" + boardName);
        MyLog.i(TAG, "bootloader-" + bootloader);
        MyLog.i(TAG, "brand-" + brand);
        MyLog.i(TAG, "cpuAbi-" + cpuAbi);
        MyLog.i(TAG, "device-" + device);
        MyLog.i(TAG, "display-" + display);
        MyLog.i(TAG, "fingerprint-" + fingerprint);
        MyLog.i(TAG, "hardWare-" + hardWare);
        MyLog.i(TAG, "host-" + host);
        MyLog.i(TAG, "id-" + id);
        MyLog.i(TAG, "model-" + model);
        MyLog.i(TAG, "manufactuere-" + manufactuere);
        MyLog.i(TAG, "product-" + product);
        MyLog.i(TAG, "tags-" + tags);
        MyLog.i(TAG, "time-" + time);
        MyLog.i(TAG, "type-" + type);
        MyLog.i(TAG, "user-" + user);
        MyLog.i(TAG, "versionRelease-" + versionRelease);
        MyLog.i(TAG, "versionCodename-" + versionCodename);
        MyLog.i(TAG, "versionIncremental-" + versionIncremental);
        MyLog.i(TAG, "sdk-" + sdk);
        MyLog.i(TAG, "sdkInt-" + sdkInt);
    }

    @Override
    public void evak(String function) {
        String func = "javascript:" + function;
        MyLog.i(TAG, "evak " + func);
        runOnUiThread(() -> {
            if (SYSTEM_VERSION < 18) {
                webView.loadUrl(func);
            } else {
                webView.evaluateJavascript(func, value -> {

                });
            }
        });
    }


    Long exitTime = 0L;
    boolean firstBack = true;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                if ((System.currentTimeMillis() - exitTime) > 2000 && firstBack) {
                    String msg = "再按一次返回键退出";
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    // 计算两次返回键按下的时间差
                    exitTime = System.currentTimeMillis();
                    firstBack = false;
                } else {
                    // 关闭应用程序
                    finish();
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
