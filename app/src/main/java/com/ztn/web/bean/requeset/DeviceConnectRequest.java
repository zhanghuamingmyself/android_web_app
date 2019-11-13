package com.ztn.web.bean.requeset;


import com.ztn.web.bean.response.DeviceConnectResponse;

import java.util.List;

import lombok.Data;

/**
 * @author zhm
 */
@Data
public class DeviceConnectRequest {
    private String deviceCore;
    private String ip;
    private String address;
    private String appKey;
    private String appSecret;
    private String appVersion;
    private String systemVersion = android.os.Build.VERSION.RELEASE;
    private String systemType = "android";
    private String activityKey;
    private Long time = System.currentTimeMillis();
    private Integer widthPixels;
    private Integer heightPixels;
    private Float xdpi;
    private Float ydpi;
    private Boolean cleanAll;
    private String systemCompany = android.os.Build.MANUFACTURER;
    private List<DeviceConnectResponse.Activity> activityList;
}
