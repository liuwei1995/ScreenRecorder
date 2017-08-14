package com.liuwei1995.screenrecorder.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.liuwei1995.screenrecorder.service.ScreenRecordService;

/**
 * Created by liuwei on 2017/8/11 16:54
 */

public class ScreenRecordActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getScreenBaseInfo();
            startScreenRecording();
        }else {
            finish();
        }
    }
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;


    /**
     * 获取屏幕相关数据
     */
    private void getScreenBaseInfo() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mScreenDensity = metrics.densityDpi;
    }

    private static final int REQUEST_CODE = 1000;

    /**
     * 获取屏幕录制的权限
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScreenRecording() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(permissionIntent, REQUEST_CODE);
    }

    /** 是否已经开启视频录制 */
    private boolean isStarted = false;
    /** 是否为标清视频 */
    private boolean isVideoSd = true;
    /** 是否开启音频录制 */
    private boolean isAudio = true;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                // 获得权限，启动Service开始录制
                Intent service = new Intent(this, ScreenRecordService.class);
                service.putExtra("code", resultCode);
                service.putExtra("data", data);
                service.putExtra("audio", isAudio);
                service.putExtra("width", mScreenWidth);
                service.putExtra("height", mScreenHeight);
                service.putExtra("density", mScreenDensity);
                service.putExtra("quality", isVideoSd);
                startService(service);
                // 已经开始屏幕录制，修改UI状态
                isStarted = !isStarted;
//                mButton.setText("停止");
//                simulateHome(); // this.finish();  // 可以直接关闭Activity
            } else {
                Toast.makeText(this, "取消或没有权限", Toast.LENGTH_SHORT).show();
            }
        }
    }




}
