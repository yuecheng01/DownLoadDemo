package com.yeucheng.downloaddemo;

/**
 * Created by Administrator on 2018/3/27.
 */

public interface IDownLoadListener {
     void onProgress(int progress);
     void onSuccess();
     void onFailed();
     void onPause();
     void onCanceled();
}
