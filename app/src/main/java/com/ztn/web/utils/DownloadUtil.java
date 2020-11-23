package com.ztn.web.utils;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.ztn.web.bean.TaskStatus;
import com.ztn.web.bean.call.IDownloadCallBack;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DownloadUtil {
    private final static String TAG = DownloadUtil.class.getSimpleName();

    private static Map<Integer, String> Mstatues = new HashMap<>();
    private static Map<Integer, String> MtotalBytes = new HashMap<>();
    private static Map<Integer, String> MPath = new HashMap<>();


    /**
     * 建立下载任务
     *
     * @param url
     * @param saveName
     */

    public int createTask(String url, String saveName, IDownloadCallBack callBack) {
        return createDownloadTask(url, saveName, callBack);
    }

    /**
     * 获取下载任务状态
     */

    public TaskStatus checkTaskStatus(int taskId) {
        try {
            TaskStatus status = new TaskStatus();
            status.setDownLength(Mstatues.get(taskId));
            status.setLength(MtotalBytes.get(taskId));
            status.setTaskId("" + taskId);
            return status;
        } catch (Exception e) {
            e.printStackTrace();
            TaskStatus status = new TaskStatus();
            status.setDownLength("0");
            status.setLength("0");
            status.setTaskId("0");
            return status;
        }

    }

    /**
     * 取消下载任务
     * cancelTask
     *
     * @return
     */
    public int cancelTask(int taskId) {
        try {
            FileDownloader.getImpl().pause(taskId);
            new File(MPath.get(taskId)).delete();
            new File(FileDownloadUtils.getTempPath(MPath.get(taskId))).delete();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    private int createDownloadTask(String url, String path, IDownloadCallBack callBack) {
        boolean isDir = false;
        final int id = FileDownloader.getImpl().create(url)
                .setPath(path, isDir)
                .setCallbackProgressTimes(300)
                .setMinIntervalUpdateSpeed(400)
                .setListener(new FileDownloadSampleListener() {

                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.pending(task, soFarBytes, totalBytes);
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.progress(task, soFarBytes, totalBytes);
                        Mstatues.put(task.getId(), "" + soFarBytes);
                        callBack.progress(task.getId(), soFarBytes, totalBytes);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        super.error(task, e);
                        MPath.remove(task.getId());
                        Mstatues.remove(task.getId());
                        MtotalBytes.remove(task.getId());
                        callBack.error(task.getId(), e.getMessage());
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                        MtotalBytes.put(task.getId(), "" + totalBytes);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.paused(task, soFarBytes, totalBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        super.completed(task);
                        MPath.remove(task.getId());
                        Mstatues.remove(task.getId());
                        MtotalBytes.remove(task.getId());
                        callBack.completed(task.getId());
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        super.warn(task);
                        LogUtil.i(TAG, "-----------warn---------" + task);
                        callBack.error(task.getId(), "warn");
                    }
                }).start();
        MPath.put(id, path);
        return id;
    }


}
