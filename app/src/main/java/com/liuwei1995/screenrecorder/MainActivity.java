package com.liuwei1995.screenrecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.liuwei1995.screenrecorder.service.DesktopViewTimerService;
import com.liuwei1995.screenrecorder.service.ScreenRecordService;
import com.liuwei1995.screenrecorder.util.FloatWindowPermissionUtils;
import com.liuwei1995.screenrecorder.util.permission.AndPermission;
import com.liuwei1995.screenrecorder.util.permission.PermissionListener;
import com.liuwei1995.screenrecorder.util.permission.RationaleListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mButton;
    private static final String RECORD_STATUS = "record_status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        if(savedInstanceState != null) {
            isStarted = savedInstanceState.getBoolean(RECORD_STATUS);
        }
        setButtonText();
        getScreenBaseInfo();
        AndPermission.with(this)
                .setPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                .setCallback(new PermissionListener() {
                    @Override
                    protected void onSucceed(Context context, int requestCode, @NonNull List<String> grantPermissions) {

                    }

                    @Override
                    protected void onFailed(@NonNull Context context, int requestCode, List<String> deniedPermissionsList, List<String> deniedDontRemindList, RationaleListener rationale) {
                        if (deniedDontRemindList != null && deniedDontRemindList.size() > 0){
                            rationale.showSettingDialog(context,rationale,deniedDontRemindList);
                        }else {
                            Toast.makeText(context, "权限不够", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .start();
    }

    private void setButtonText() {
        if (mButton != null)
        if (DesktopViewTimerService.isStart){
            mButton.setText("关闭");
        }else {
            mButton.setText("开始");
        }
    }

    private static final int REQUEST_CODE = 1000;

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
                mButton.setText("停止");
                simulateHome(); // this.finish();  // 可以直接关闭Activity
            } else {
                Toast.makeText(this, "取消或没有权限", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == 10 && resultCode == Activity.RESULT_OK){
            if (FloatWindowPermissionUtils.checkFloatWindowPermission(this)){
                initDesktopView();
            }else {
                Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(RECORD_STATUS, isStarted);
    }

    /**
     * 关闭屏幕录制，即停止录制Service
     */
    private void stopScreenRecording() {
        Intent service = new Intent(this, ScreenRecordService.class);
        stopService(service);
        isStarted = !isStarted;
    }

    /**
     * 模拟HOME键返回桌面的功能
     */
    private void simulateHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 在这里将BACK键模拟了HOME键的返回桌面功能（并无必要）
//        if(keyCode == KeyEvent.KEYCODE_BACK) {
//            simulateHome();
//            return true;
//        }
        //方式一：将此任务转向后台
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        //方式二：返回手机的主屏幕
    /*Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addCategory(Intent.CATEGORY_HOME);
    startActivity(intent);*/
        return super.onKeyDown(keyCode,event);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                if (DesktopViewTimerService.isStart){
                    stopScreenRecording();
                }else {
                    startScreenRecording();
                }
//                if(isStarted) {
//                    mButton.setText("开始");
//                    stopScreenRecording();
//                } else {
//                    startScreenRecording();
//                }
                break;
            case R.id.button2:
                permissionDesktopView();
                break;
            default:break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setButtonText();
    }

    @Override
    protected void onDestroy() {
        stopScreenRecording();
        super.onDestroy();
    }



    private synchronized void permissionDesktopView() {
        if (!FloatWindowPermissionUtils.checkFloatWindowPermission(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,10);
            }else {
                Toast.makeText(this, "请手动去打开悬浮窗口权限", Toast.LENGTH_SHORT).show();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
                }
            }
        }else {
            initDesktopView();
        }
    }

    public void initDesktopView(){
        DesktopViewTimerService.startService(this);
    }


}
