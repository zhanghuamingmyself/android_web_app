/*
 * Copyright 2018 Zhenjie Yan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ztn.web.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.ztn.web.App;
import com.ztn.web.AppConfig;
import com.ztn.web.bean.FileConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Zhenjie Yan on 2018/6/9.
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    private static FileUtils instance;
    private static final int SUCCESS = 1;
    private static final int FAILED = 0;
    private Context context;
    private FileOperateCallback callback;
    private volatile boolean isSuccess;
    private String errorStr;

    public static FileUtils getInstance(Context context) {
        if (instance == null)
            instance = new FileUtils(context);
        return instance;
    }

    private FileUtils(Context context) {
        this.context = context;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (callback != null) {
                if (msg.what == SUCCESS) {
                    callback.onSuccess();
                }
                if (msg.what == FAILED) {
                    callback.onFailed(msg.obj.toString());
                }
            }
        }
    };

    public FileUtils copyAssetsToSD(final String srcPath, final String sdPath) {
        copyAssetsToDst(context, srcPath, sdPath);
        if (isSuccess)
            handler.obtainMessage(SUCCESS).sendToTarget();
        else
            handler.obtainMessage(FAILED, errorStr).sendToTarget();
        return this;
    }

    public void setFileOperateCallback(FileOperateCallback callback) {
        this.callback = callback;
    }

    private void copyAssetsToDst(Context context, String srcPath, String dstPath) {
        Log.i("lala", dstPath);
        try {
            String fileNames[] = context.getAssets().list(srcPath);
            if (fileNames.length > 0) {
                File file = new File(dstPath);
                if (!file.exists()) file.mkdirs();
                for (String fileName : fileNames) {
                    if (!srcPath.equals("")) { // assets 文件夹下的目录
                        copyAssetsToDst(context, srcPath + File.separator + fileName, dstPath + File.separator + fileName);
                    } else { // assets 文件夹
                        copyAssetsToDst(context, fileName, dstPath + File.separator + fileName);
                    }
                }
            } else {
                File outFile = new File(dstPath);
                InputStream is = context.getAssets().open(srcPath);
                FileOutputStream fos = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            errorStr = e.getMessage();
            isSuccess = false;
        }
    }

    public interface FileOperateCallback {
        void onSuccess();

        void onFailed(String error);
    }

    public static FileConfig readCofigFile(ContentResolver resolver) {
        try {
            File path = new File(AppConfig.CONFIG_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            File[] files = path.listFiles();
            if (null == files) {
                return null;
            }
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                String fileName = file.getName();
                String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                if ("cfg".equals(suffix)) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    String cfgStr;
                    StringBuffer stringBuffer = new StringBuffer();
                    while (true) {
                        if (!(null != (cfgStr = bufferedReader.readLine()))) break;
                        stringBuffer.append(cfgStr);
                    }
                    bufferedReader.close();
                    FileConfig fileConfig = App.getInstance().getGson().fromJson(stringBuffer.toString(), FileConfig.class);
                    if (null == fileConfig) {
                        return null;
                    }
                    if(null == fileConfig.getIsDebug()){
                        fileConfig.setIsDebug(false);
                    }
                    if (null == fileConfig.getDeviceCore()) {
                        String androidId = Settings.System.getString(resolver, Settings.System.ANDROID_ID);
                        fileConfig.setDeviceCore(androidId);
                        cfgStr = App.getInstance().getGson().toJson(fileConfig);
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                        bufferedWriter.write(cfgStr);
                        bufferedWriter.flush();
                        bufferedWriter.close();
                    }

                    return fileConfig;
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean delFile(File file) {
        MyLog.e(TAG,"删除文件"+file.getAbsolutePath());
        if (!file.exists()) {
            return false;
        }

        if (file.isFile()) {
            return file.delete();
        } else {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f);
            }
            return file.delete();
        }
    }

}