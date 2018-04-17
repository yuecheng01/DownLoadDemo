package com.yeucheng.downloaddemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.yeucheng.downloaddemo.permission.PermissionFail;
import com.yeucheng.downloaddemo.permission.PermissionHelper;
import com.yeucheng.downloaddemo.permission.PermissionSuccess;

import java.io.File;

import cn.jzvd.JZVideoPlayerStandard;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mStartDownload;
    private Button mPauseDownload;
    private Button mCanceleDownload;
    private Button mPlay;
    private boolean click = false;
    private JZVideoPlayerStandard mPlayer;
    SketchView mSketchView;

    String directory = Environment.getExternalStoragePublicDirectory(Environment
            .DIRECTORY_DOWNLOADS).getPath();
    private DownloadService.DownloadBinder mDownloadBinder;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDownloadBinder = (DownloadService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mSketchView.startAnimation();
        event();
    }

    private void event() {
        Intent intent = new Intent(this, DownloadService.class);
        //启动服务
        startService(intent);
        //绑定服务
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        PermissionHelper.requestPermisson(MainActivity.this, 11, new
                String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        initVideo();
    }

    private void initVideo() {
        File file = new File(directory, "jilejintu.mp4");
        Log.d("path", file.getPath());
        if (!file.exists()) {
            Toast.makeText(this, "将要播放的文件不存在!请先下载", Toast.LENGTH_SHORT).show();
            click = false;
        } else {
            click = true;
            mPlayer.setUp(file.getPath(),JZVideoPlayerStandard
                    .SCREEN_WINDOW_NORMAL, "饺子闭眼睛");
        }
        if (click){
            mPlay.setClickable(true);
        }else {
            mPlay.setClickable(false);
        }
    }

    @PermissionFail(requestCode = 11)
    public void permissionFailed() {
        Toast.makeText(this, "请在系统设置中给予权限", Toast.LENGTH_SHORT).show();
    }

    @PermissionSuccess(requestCode = 11)
    public void permissionSuccess() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionHelper.requestPermissionsResult(requestCode, 11, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    private void init() {
        mStartDownload = findViewById(R.id.start);
        mPauseDownload = findViewById(R.id.pause);
        mCanceleDownload = findViewById(R.id.cancled);
        mPlay = findViewById(R.id.play);
        mPlayer = findViewById(R.id.video);
        mSketchView = findViewById(R.id.sto);

        mStartDownload.setOnClickListener(this);
        mPauseDownload.setOnClickListener(this);
        mCanceleDownload.setOnClickListener(this);
        mPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mDownloadBinder == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.start:
                String url = "http://192.168.3.130/jilejintu.mp4";
                mDownloadBinder.startDownload(url);
                break;
            case R.id.pause:
                mDownloadBinder.pauseDownload();
                break;
            case R.id.cancled:
                mDownloadBinder.canclelDownload();
                break;
            case R.id.play:
                if (!mFlag) {

                    mPlay.setText("点击暂停");
                    mFlag = true;
                } else {
                    mPlayer.releaseAllVideos();
                    mPlay.setText("点击继续");
                    mFlag = false;
                }
                break;
        }
    }

    private boolean mFlag = false;
}
