package com.liuwei1995.screenrecorder;

import android.content.Context;


/**
 * Created by dell on 2017/3/24
 */

public final class App {

    private  Context mContext;

    public static Context getContext() {
        return newInstance().mContext;
    }

    private App() {
    }


    private static App app;

    public static App newInstance() {
        if(app == null){
            synchronized (App.class){
                if(app == null)app = new App();
            }
        }
        return app;
    }

}
