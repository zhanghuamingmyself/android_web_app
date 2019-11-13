package com.ztn.web.javascript_interface;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.hb.dialog.dialog.ConfirmDialog;
import com.hb.dialog.myDialog.ActionSheetDialog;
import com.tencent.bugly.beta.Beta;
import com.ztn.web.MainActivity;
import com.ztn.web.R;
import com.ztn.web.utils.AppUtils;
import com.ztn.web.utils.GsonUtil;

import java.util.HashMap;
import java.util.Map;

public class CommonMethod {
    private final static String TAG = CommonMethod.class.getSimpleName();

    private MainActivity activity;

    public CommonMethod(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void showNativeMenu() {
        ActionSheetDialog dialog = new ActionSheetDialog(activity).builder().setTitle("请选择")
                .addSheetItem("监测更新", null, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        Beta.checkUpgrade();//检查版本号
                    }
                }).addSheetItem("当前APP版本", null, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        String versionName = AppUtils.getVersionName(activity);
                        Toast.makeText(activity, versionName, Toast.LENGTH_LONG).show();
                    }
                }).addSheetItem("关于我们", null, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(activity);
                        confirmDialog.setLogoImg(R.mipmap.ic_launcher).setMsg("中泰能公司");
                        confirmDialog.setClickListener(new ConfirmDialog.OnBtnClickListener() {
                            @Override
                            public void ok() {

                            }

                            @Override
                            public void cancel() {

                            }
                        });
                        confirmDialog.show();
                    }
                });
        dialog.show();
    }

    @JavascriptInterface
    public String sysInfo() {
        Map<String, Object> sysInfo = new HashMap<>();
        sysInfo.put("appName", AppUtils.getAppName(activity));
        sysInfo.put("versionName", AppUtils.getVersionName(activity));
        sysInfo.put("showMenu", true);
        sysInfo.put("browserType", "webView");
        sysInfo.put("brand", android.os.Build.BRAND);
        String info = GsonUtil.BeanToJson(sysInfo);
        Log.i(MainActivity.class.getSimpleName(), info);
        return info;
    }

    /**
     * 可视化日志打
     *
     * @param str
     */
    @JavascriptInterface
    public void textLog(String str) {
        activity.textLog(str);
    }


    @JavascriptInterface
    public void setDebug(String isDebug) {
        if (null == isDebug || Boolean.parseBoolean(isDebug)){
            activity.showDevTool();
        }else {
            activity.hideDevTool();
        }
    }
}
