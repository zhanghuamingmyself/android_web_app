package com.ztn.web.bean.requeset;


import com.ztn.web.bean.response.DeviceConnectResponse;

import java.util.List;


/**
 * @author zhm
 */

public class DeviceConnectRequest {
    private String deviceCore;
    private String ip;
    private String address;
    private String appVersion;
    private String systemVersion = android.os.Build.VERSION.RELEASE;
    private String systemType = "android";
    private Integer widthPixels;
    private Integer heightPixels;
    private Float xdpi;
    private Float ydpi;
    private String systemCompany = android.os.Build.MANUFACTURER;

    private String appKey;
    private String appSecret;

    private List<DeviceConnectResponse.Activity> activityList;

    public String getDeviceCore() {
        return deviceCore;
    }

    public void setDeviceCore(String deviceCore) {
        this.deviceCore = deviceCore;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public Integer getWidthPixels() {
        return widthPixels;
    }

    public void setWidthPixels(Integer widthPixels) {
        this.widthPixels = widthPixels;
    }

    public Integer getHeightPixels() {
        return heightPixels;
    }

    public void setHeightPixels(Integer heightPixels) {
        this.heightPixels = heightPixels;
    }

    public Float getXdpi() {
        return xdpi;
    }

    public void setXdpi(Float xdpi) {
        this.xdpi = xdpi;
    }

    public Float getYdpi() {
        return ydpi;
    }

    public void setYdpi(Float ydpi) {
        this.ydpi = ydpi;
    }

    public String getSystemCompany() {
        return systemCompany;
    }

    public void setSystemCompany(String systemCompany) {
        this.systemCompany = systemCompany;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public List<DeviceConnectResponse.Activity> getActivityList() {
        return activityList;
    }

    public void setActivityList(List<DeviceConnectResponse.Activity> activityList) {
        this.activityList = activityList;
    }


}
