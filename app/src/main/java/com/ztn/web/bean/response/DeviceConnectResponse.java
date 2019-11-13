package com.ztn.web.bean.response;

import java.util.List;

import lombok.Data;

/**
 * @author zhm
 */
@Data
public class DeviceConnectResponse {
    private Device device;
    private List<Activity> addActivityList;
    private List<Activity> delActivityList;
    private List<Activity> openActivityList;
    private List<Activity> closeActivityList;
    private List<Activity> updateActivityList;

    @Data
    public static class Activity {
        private String activityName;
        private Boolean isLocalhost;
        private String activityKey;
        private String outsideIndex;
        private String version;
        private Boolean needNet;
        private Boolean open;
        private String previewPath;
        private Boolean isShowDialog;

    }

    @Data
    public static class Device{
        private String systemVersion;
        private String systemType;
        private String activityKey;
        private String ip;
        private String address;
        private String deviceCore;
        private String appVersion;
        private Integer widthPixels;
        private Integer heightPixels;
        private Float xdpi;
        private Float ydpi;
        private Float widthSize;
        private Float heightSize;
        private Double screenSize;
        private Boolean cleanAll;
        private Boolean showDevTool;
        private Integer connectDelay;
    }

}
