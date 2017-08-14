package com.liuwei1995.screenrecorder.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.liuwei1995.screenrecorder.R;

/**
 * Created by liuwei on 2017/8/14 15:25
 */

public class DesktopViewTimerService extends Service implements View.OnTouchListener,View.OnClickListener{


    public static synchronized void startService(Context context) {
        DesktopViewTimerService.isStart = true;
        Intent intent = new Intent(context,DesktopViewTimerService.class);
        context.startService(intent);
    }
    public static boolean isStart = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private View desktopView;
    private WindowManager wm;
    private WindowManager.LayoutParams layoutParams;

    private float rawX;
    private float rawY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                rawX = event.getRawX();
                rawY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                //getRawX/Y 是获取相对于Device的坐标位置 注意区别getX/Y[相对于View]
                layoutParams.x = layoutParams.x + (int) (event.getRawX() - rawX);
                layoutParams.y = layoutParams.y + (int) (event.getRawY() - rawY);
                rawX = event.getRawX();
                rawY = event.getRawY();
                //更新"桌面歌词"的位置
                wm.updateViewLayout(desktopView,layoutParams);
                //下面的removeView 可以去掉"桌面歌词"
                //wm.removeView(myView);
                break;
            case MotionEvent.ACTION_MOVE:
                layoutParams.x = layoutParams.x + (int) (event.getRawX() - rawX);
                layoutParams.y = layoutParams.y + (int) (event.getRawY() - rawY);
                rawX = event.getRawX();
                rawY = event.getRawY();
                wm.updateViewLayout(desktopView,layoutParams);
                break;
        }
        return false;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isStart = true;
        initDesktopView();
        return super.onStartCommand(intent, flags, startId);
    }



    private Chronometer mChronometer;

    private static final String TAG = "DesktopViewTimerService";

    public void initDesktopView(){
        if (desktopView == null){
            desktopView = LayoutInflater.from(this).inflate(R.layout.desktop_view_timer_service, null);
            mChronometer = (Chronometer) desktopView.findViewById(R.id.timer);
            desktopView.findViewById(R.id.tv_stop).setOnClickListener(this);
            mChronometer.setBase(SystemClock.elapsedRealtime());
//            mChronometer.setFormat("00:00");
            mChronometer.setFormat("%s");
            mChronometer.start();
            wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            //设置TextView的属性
            layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            //这里是关键，使控件始终在最上方
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //这个Gravity也不能少，不然的话，下面"移动歌词"的时候就会出问题了～ 可以试试[官网文档有说明]
            layoutParams.gravity = Gravity.LEFT|Gravity.TOP;

            //监听 OnTouch 事件 为了实现"移动歌词"功能
            desktopView.setOnTouchListener(this);

            wm.addView(desktopView, layoutParams);
        } else {
            wm.removeView(desktopView);
            wm.addView(desktopView, layoutParams);
        }
    }

    @Override
    public void onClick(View v) {
        Intent service = new Intent(this, ScreenRecordService.class);
        stopService(service);
        stopSelf();
    }

    public void hideDesktopView(){
        if (wm != null && desktopView != null)
            wm.removeView(desktopView);
    }
    @Override
    public void onDestroy() {
        isStart = false;
        hideDesktopView();
        super.onDestroy();
    }
}
