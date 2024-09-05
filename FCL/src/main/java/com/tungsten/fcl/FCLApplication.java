package com.tungsten.fcl;

import android.app.*;
import android.content.*;
import android.os.*;
import androidx.annotation.*;
import com.tungsten.fcl.util.DeviceInfoUtils;
import com.tungsten.fclauncher.utils.*;
import java.lang.ref.WeakReference;
import java.util.Properties;

public class FCLApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static WeakReference<Activity> currentActivity;
    public static Properties appConfig;
    public static DeviceInfoUtils deviceInfoUtils;
    private static SharedPreferences sharedPreferences;

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
        deviceInfoUtils = new DeviceInfoUtils(this);
        sharedPreferences = getApplicationContext().getSharedPreferences("launcher", Context.MODE_PRIVATE);
        FCLPath.loadPaths(this);
        appConfig = FCLPath.APP_CONFIG_PROPERTIES;

        this.registerActivityLifecycleCallbacks(this);
    }

    public static Activity getCurrentActivity() {
        return currentActivity.get();
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    private void enabledStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectNetwork().detectCustomSlowCalls().detectDiskReads().detectDiskWrites().detectAll().penaltyLog().build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().detectActivityLeaks().detectAll().penaltyLog().build());
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
        if (currentActivity.get() == activity) currentActivity = null;
    }
}