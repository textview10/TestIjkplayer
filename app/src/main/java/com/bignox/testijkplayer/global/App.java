package com.bignox.testijkplayer.global;

import android.app.Application;

import com.test.ijkplayer_view.IjkPlayerManager;

/**
 * @author xu.wang
 * @date 2019/4/28 17:06
 * @desc
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        IjkPlayerManager.init();
    }
}
