package com.liuwei1995.screenrecorder;

import android.app.Application;
import android.os.Build;


/**
 * Created by liuwei on 2017/3/20
 */

public class BaseApplication extends Application{

    public static final int SDK = Build.VERSION.SDK_INT;
    /**
     * 网络是否可用
     */
    public static boolean NETWORK_IS_AVAILABLE = false;

    public static boolean isWiFi = false;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashUtils.getInstance().init(this);
    }

}
