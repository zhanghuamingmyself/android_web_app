package com.ztn.web.javascript_interface;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import com.hb.dialog.dialog.ConfirmDialog;
import com.hb.dialog.myDialog.ActionSheetDialog;
import com.ztn.web.MainActivity;
import com.ztn.web.R;
import com.ztn.web.ShowMain;
import com.ztn.web.utils.AppUtils;
import com.ztn.web.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class CommonMethod {
    private final static String TAG = CommonMethod.class.getSimpleName();

    private ShowMain showMain;

    public CommonMethod(ShowMain showMain) {
        this.showMain = showMain;
    }

    @JavascriptInterface
    public void showNativeMenu() {
        ActionSheetDialog dialog = new ActionSheetDialog(showMain.appCompatActivity).builder().setTitle("请选择")
                .addSheetItem("当前APP版本", null, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        String versionName = AppUtils.getVersionName(showMain.appCompatActivity);
                        Toast.makeText(showMain.appCompatActivity, versionName, Toast.LENGTH_LONG).show();
                    }
                }).addSheetItem("关于我们", null, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        ConfirmDialog confirmDialog = new ConfirmDialog(showMain.appCompatActivity);
                        confirmDialog.setLogoImg(R.mipmap.ic_launcher).setMsg("iot");
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
        sysInfo.put("appName", AppUtils.getAppName(showMain.appCompatActivity));
        sysInfo.put("versionName", AppUtils.getVersionName(showMain.appCompatActivity));
        sysInfo.put("showMenu", true);
        sysInfo.put("browserType", "webView");
        sysInfo.put("brand", android.os.Build.BRAND);
        String info = JsonUtil.toJson(sysInfo);
        Log.i(MainActivity.class.getSimpleName(), info);
        return info;
    }

}
