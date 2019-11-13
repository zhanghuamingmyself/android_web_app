package com.ztn.web.javascript_interface;

import android.content.Context;
import android.media.AudioManager;
import android.webkit.JavascriptInterface;


public class VolumeMethod {
    private final static String TAG = VolumeMethod.class.getSimpleName();
    private Context mContext = null;
    private AudioManager mgr;

    public VolumeMethod(Context mContext) {
        this.mContext = mContext;
        mgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }


    @JavascriptInterface
    public int getMediaValue() {
        return  mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @JavascriptInterface
    public int setMediaValue(int val) {
        // 调低音量
        mgr.setStreamVolume(AudioManager.STREAM_MUSIC,val,0);
        return 1;
    }
}
