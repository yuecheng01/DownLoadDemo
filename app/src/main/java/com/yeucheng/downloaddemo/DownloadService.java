package com.yeucheng.downloaddemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {
    private DownloadTask mDownloadTask;
    private String mDownloadUrl;
    private IDownLoadListener mListener = new IDownLoadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            mDownloadTask = null;
            //下载成功时将前台服务通知关闭,并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download success", -1));
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            mDownloadTask = null;
            //下载失败时将前台服务通知关闭,,并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause() {
            mDownloadTask = null;
            Toast.makeText(DownloadService.this, "Download onPause", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            mDownloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Download onCanceled", Toast.LENGTH_SHORT).show();
        }
    };

    class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (mDownloadTask == null) {
                mDownloadUrl = url;
                mDownloadTask = new DownloadTask(mListener);
                mDownloadTask.execute(mDownloadUrl);
                startForeground(1, getNotification("Downloading...", 0));
                Toast.makeText(DownloadService.this, "Downloading...", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (mDownloadTask != null) {
                mDownloadTask.pauseDownload();
            }
        }

        public void canclelDownload() {
            if (mDownloadTask != null) {
                //取消下载时需将文件删除,并将通知关闭
                String fileName = mDownloadUrl.substring(mDownloadUrl.lastIndexOf("/"));
                String direCtory = Environment.getExternalStoragePublicDirectory(Environment
                        .DIRECTORY_DOWNLOADS).getPath();
                File file = new File(direCtory + fileName);
                if (file.exists()) {
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public DownloadService() {
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(title);
        builder.setContentIntent(pi);
        if (progress >= 0) {
            //当progress大于0是才需要显示下载进度
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }

    private DownloadBinder mDownloadBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mDownloadBinder;
    }
}
