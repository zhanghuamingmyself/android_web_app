package com.ztn.web.bean.response;

import java.util.List;


/**
 * @author zhm
 */

public class DeviceConnectResponse {
    private Device device;
    private List<Activity> addActivityList;
    private List<Activity> deleteActivityList;
    private List<Activity> openActivityList;
    private List<Activity> closeActivityList;
    private List<Activity> updateActivityList;


    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public List<Activity> getAddActivityList() {
        return addActivityList;
    }

    public void setAddActivityList(List<Activity> addActivityList) {
        this.addActivityList = addActivityList;
    }

    public List<Activity> getDeleteActivityList() {
        return deleteActivityList;
    }

    public void setDeleteActivityList(List<Activity> deleteActivityList) {
        this.deleteActivityList = deleteActivityList;
    }

    public List<Activity> getOpenActivityList() {
        return openActivityList;
    }

    public void setOpenActivityList(List<Activity> openActivityList) {
        this.openActivityList = openActivityList;
    }

    public List<Activity> getCloseActivityList() {
        return closeActivityList;
    }

    public void setCloseActivityList(List<Activity> closeActivityList) {
        this.closeActivityList = closeActivityList;
    }

    public List<Activity> getUpdateActivityList() {
        return updateActivityList;
    }

    public void setUpdateActivityList(List<Activity> updateActivityList) {
        this.updateActivityList = updateActivityList;
    }

    public static class Activity {
        private Integer activityId;

        private Integer appId;

        private String name;

        private String activityKey;

        private Integer activityVersionId;

        private String indexFile;

        private String filePath;

        private String version;

        private Boolean disable;

        private String path;


        public Integer getActivityId() {
            return activityId;
        }

        public void setActivityId(Integer activityId) {
            this.activityId = activityId;
        }

        public Integer getAppId() {
            return appId;
        }

        public void setAppId(Integer appId) {
            this.appId = appId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getActivityKey() {
            return activityKey;
        }

        public void setActivityKey(String activityKey) {
            this.activityKey = activityKey;
        }

        public Integer getActivityVersionId() {
            return activityVersionId;
        }

        public void setActivityVersionId(Integer activityVersionId) {
            this.activityVersionId = activityVersionId;
        }

        public String getIndexFile() {
            return indexFile;
        }

        public void setIndexFile(String indexFile) {
            this.indexFile = indexFile;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Boolean getDisable() {
            return disable;
        }

        public void setDisable(Boolean disable) {
            this.disable = disable;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }


    public static class Device {
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

        public String getActivityKey() {
            return activityKey;
        }

        public void setActivityKey(String activityKey) {
            this.activityKey = activityKey;
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

        public String getDeviceCore() {
            return deviceCore;
        }

        public void setDeviceCore(String deviceCore) {
            this.deviceCore = deviceCore;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
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

        public Float getWidthSize() {
            return widthSize;
        }

        public void setWidthSize(Float widthSize) {
            this.widthSize = widthSize;
        }

        public Float getHeightSize() {
            return heightSize;
        }

        public void setHeightSize(Float heightSize) {
            this.heightSize = heightSize;
        }

        public Double getScreenSize() {
            return screenSize;
        }

        public void setScreenSize(Double screenSize) {
            this.screenSize = screenSize;
        }
    }

}
