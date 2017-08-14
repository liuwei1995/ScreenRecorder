package com.liuwei1995.screenrecorder.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liuwei on 2017/8/11 15:10
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenRecordService extends Service {

    private static final String TAG = "ScreenRecordingService";

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private int mResultCode;
    private Intent mResultData;
    /** 是否为标清视频 */
    private boolean isVideoSd;
    /** 是否开启音频录制 */
    private boolean isAudio;

    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;

    private ScreenRecordServiceReceiver mScreenRecordServiceReceiver;

    /**暂停*/
    public static final String ACTION_RECEIVER_PAUSE = "ACTION_RECEIVER_PAUSE";
    /**继续*/
    public static final String ACTION_RECEIVER_RESUME = "ACTION_RECEIVER_RESUME";
    /**停止*/
    public static final String ACTION_RECEIVER_STOP = "ACTION_RECEIVER_STOP";

    @Override
    public void onCreate() {
        super.onCreate();
        mScreenRecordServiceReceiver = new ScreenRecordServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECEIVER_PAUSE);
        filter.addAction(ACTION_RECEIVER_RESUME);
        filter.addAction(ACTION_RECEIVER_STOP);
        registerReceiver(mScreenRecordServiceReceiver,filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopDesktopViewTimerService();
        DesktopViewTimerService.startService(this);
        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        mScreenWidth = intent.getIntExtra("width", 720);
        mScreenHeight = intent.getIntExtra("height", 1280);
        mScreenDensity = intent.getIntExtra("density", 1);
        isVideoSd = intent.getBooleanExtra("quality", true);
        isAudio = intent.getBooleanExtra("audio", true);
        mMediaProjection =  createMediaProjection();
        mMediaRecorder = createMediaRecorder();
        mVirtualDisplay = createVirtualDisplay(); // 必须在mediaRecorder.prepare() 之后调用，否则报错"fail to get surface"
        mMediaRecorder.start();
        return Service.START_NOT_STICKY;
    }

    private MediaProjection createMediaProjection() {
        return ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(mResultCode, mResultData);
    }

    private MediaRecorder createMediaRecorder() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date curDate = new Date(System.currentTimeMillis());
        String curTime = formatter.format(curDate).replace(" ", "");
        String videoQuality = "HD";
        if(isVideoSd) videoQuality = "SD";

        MediaRecorder mediaRecorder = new MediaRecorder();
        if(isAudio) mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(new File(Environment.getExternalStorageDirectory(),videoQuality+"_" + curTime + ".mp4").toString());
//        mediaRecorder.setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + videoQuality + curTime + ".mp4");
        mediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);  //after setVideoSource(), setOutFormat()
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);  //after setOutputFormat()
        if(isAudio) mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);  //after setOutputFormat()
        int bitRate;
        if(isVideoSd) {
            mediaRecorder.setVideoEncodingBitRate(mScreenWidth * mScreenHeight);
            mediaRecorder.setVideoFrameRate(30);
            bitRate = mScreenWidth * mScreenHeight / 1000;
        } else {
            mediaRecorder.setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight);
            mediaRecorder.setVideoFrameRate(60); //after setVideoSource(), setOutFormat()
            bitRate = 5 * mScreenWidth * mScreenHeight / 1000;
        }
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Audio: " + isAudio + ", SD video: " + isVideoSd + ", BitRate: " + bitRate + "kbps");

        return mediaRecorder;
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay(TAG, mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder.getSurface(), null, null);
    }
    public void stopDesktopViewTimerService(){
        Intent service = new Intent(this, DesktopViewTimerService.class);
        stopService(service);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopDesktopViewTimerService();
        if (mScreenRecordServiceReceiver != null){
            unregisterReceiver(mScreenRecordServiceReceiver);
            mScreenRecordServiceReceiver = null;
        }
        if(mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if(mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaProjection.stop();
            mMediaRecorder.reset();
        }
        if(mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class ScreenRecordServiceReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_RECEIVER_PAUSE.equals(intent.getAction())){
                pause();
            }else if (ACTION_RECEIVER_RESUME.equals(intent.getAction())){
                resume();
            }else if (ACTION_RECEIVER_STOP.equals(intent.getAction())){
                stop();
            }
        }
    }

    public void pause(){
        if (mMediaRecorder != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaRecorder.pause();
            }
        }
    }
    public void resume(){

    }
    public void stop(){
        stopSelf();
    }

}

