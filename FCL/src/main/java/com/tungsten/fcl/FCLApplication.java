package com.tungsten.fcl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tungsten.fcl.util.PropertiesFileParse;
import com.tungsten.fclauncher.utils.FCLPath;

import java.lang.ref.WeakReference;
import java.util.Properties;

public class FCLApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private static WeakReference<Activity> currentActivity;
    public static Properties appConfig;
    public static SharedPreferences appDataSave;
    @Override
    public void onCreate() {
        // enabledStrictMode();
        super.onCreate();
        /**
         * properties文件解析必须放到全局Application
         * 因为Application的onCreate方法只会在程序启动时有且运行一次，适用于全局共享变量数据
         * 向上和向下传递值时候如果传递的是频繁访问数据可不在经过意图传递数据值
         * 解决那些频繁分配内存对象导致程序崩溃问题比如Handler...
        **/
        appConfig = new PropertiesFileParse("config.properties", getApplicationContext()).getProperties();
        appDataSave = getSharedPreferences("launcher", MODE_PRIVATE);
        FCLPath.loadPaths(this);
        if("true".equals(appConfig.getProperty("enable-private-directory-mode","false"))){
            FCLPath.SHARED_COMMON_DIR = FCLPath.PRIVATE_COMMON_DIR;
        }else{
            FCLPath.SHARED_COMMON_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + appConfig.getProperty("put-directory","FCL-Server") + "/.minecraft";
        }
        this.registerActivityLifecycleCallbacks(this);
    }

    public static Activity getCurrentActivity() {
        return currentActivity.get();
    }

    private void enabledStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectNetwork()
                .detectCustomSlowCalls()
                .detectDiskReads()
                .detectDiskWrites() 
                .detectAll()
                .penaltyLog() 
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectAll()
                .penaltyLog()
                .build());
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (currentActivity.get() == activity) {
            currentActivity = null;
        }
    }
}