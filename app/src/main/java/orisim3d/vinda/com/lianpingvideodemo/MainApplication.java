package orisim3d.vinda.com.lianpingvideodemo;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * Created by vinda on 2018/10/18.
 * Describe:
 * qq:1304314404
 */

public class MainApplication extends Application {
    public static Context mContext;
    public static Handler mHandler = new Handler();
    public static long videofram  = 0;//本地当前播放视频的帧

    public static void runUITask(Runnable run) {
        mHandler.post(run);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
