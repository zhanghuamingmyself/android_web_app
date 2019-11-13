package com.ztn.web.bean.call;

/**
 * 活动下载回调
 */
public interface IDownloadCallBack {
    void progress(Integer taskId, Integer soFarBytes, Integer totalBytes);

    void error(Integer taskId, String msg);

    void completed(Integer taskId);
}
