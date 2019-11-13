package com.ztn.web.bean.call;

/**
 * 活动解压回调
 */
public interface IUnzipCallBack {
    Long doInBackground(Void... params);
    void onPostExecute(Long result);
    void onPreExecute();
    void onProgressUpdate(Integer... values);
}
