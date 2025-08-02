package com.tungsten.fcl.util;

import static com.tungsten.fcl.setting.ConfigHolder.getSelectedPath;
import static com.tungsten.fcl.setting.ConfigHolder.innerConfig;
import static com.tungsten.fcl.util.RuntimeUtils.copyAssets;
import static com.tungsten.fcl.util.RuntimeUtils.install;
import static com.tungsten.fclauncher.utils.FCLPath.*;
import static com.tungsten.fclcore.util.io.FileUtils.batchDelete;
import static com.tungsten.fclcore.util.io.FileUtils.forceDelete;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonParseException;
import com.tungsten.fcl.setting.Config;
import com.tungsten.fcl.setting.ConfigHolder;
import com.tungsten.fcllibrary.component.theme.ThemeEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class InstallResources {
    private final CountDownLatch countDownLatch;
    private final Set<CheckFileFormat.FileInfo<?>> checkFiles;
    private final WeakReference<Activity> activityRef;
    private final WeakReference<View> needRefreshBackgroundRef;
    private final Context applicationContext;
    private volatile boolean isDestroyed = false;

    public InstallResources(Activity activity, @Nullable View needRefreshBackground) {
        if(activity == null) throw new NullPointerException("错误，无效的活动页面。请将该问题反馈给原始制造商！");
        if(needRefreshBackground != null && activity.findViewById(needRefreshBackground.getId()) == null) 
            throw new IllegalArgumentException("在"" + activity.getClass().getName() + ""视图未找到名叫"" + needRefreshBackground.getClass().getName() + ""的组件");

        this.activityRef = new WeakReference<>(activity);
        this.needRefreshBackgroundRef = new WeakReference<>(needRefreshBackground);
        this.applicationContext = activity.getApplicationContext();
        this.checkFiles = new CheckFileFormat(applicationContext).getDefaultCheckFiles();
        this.countDownLatch = new CountDownLatch(checkFiles.size() + 1);
    }

    /**
     * 检查是否已被销毁
     */
    private boolean checkDestroyed() {
        return isDestroyed || activityRef.get() == null;
    }

    /**
     * 获取Activity，如果已被回收则返回null
     */
    @Nullable
    private Activity getActivity() {
        return isDestroyed ? null : activityRef.get();
    }

    public void installGameFiles(String oldInstallDir, String srcDir, final SharedPreferences.Editor editor) throws IOException, InterruptedException {
        if (checkDestroyed()) {
            throw new IllegalStateException("InstallResources已被销毁，无法继续安装");
        }

        forceDelete(LOG_DIR, CONTROLLER_DIR, oldInstallDir); // 先删除默认目录中的按键和日志内容，如果config.json文件修改后则删除旧的config.json文件中目录资源

        countDownLatch.await(); // 等待配置文件线程关键文件操作完毕后才能继续往下操作
        
        if (checkDestroyed()) {
            throw new IllegalStateException("InstallResources已被销毁，无法继续安装");
        }
        
        install(applicationContext, getSelectedPath(innerConfig()).getAbsolutePath(), srcDir); // 安装游戏资源
        
        if(editor != null && !checkDestroyed()) {
            editor.putBoolean("isFirstInstall", false);
            editor.apply();
        }
    }

    public void installConfigFiles(String targetDir, String srcDir) throws Exception {
        if (checkDestroyed()) {
            throw new IllegalStateException("InstallResources已被销毁，无法继续安装");
        }

        batchDelete(new File(FILES_DIR), new File(CONFIG_DIR), 
                   applicationContext.getCacheDir(), applicationContext.getCodeCacheDir());

        for(CheckFileFormat.FileInfo<?> file : checkFiles) {
            if (checkDestroyed()) {
                throw new IllegalStateException("InstallResources已被销毁，安装过程中断");
            }
            
            Path externalPath = file.getExternalPath();
            try {
                copyAssets(applicationContext, file.getInternalPath(), externalPath == null ? null : externalPath.toString());
                countDownLatch.countDown(); // CountDownLatch计数器为0时，调用await()的线程不会阻塞
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("未能在APK的assets目录中找到该文件"" + file.getInternalPath() + """);
            } catch (IOException e) {
                throw new IOException("尝试读取/写入文件时发生致命错误：" + e);
            } catch (Exception e) {
                throw new Exception("未知错误：" + e);
            }
        }

        // 安全地刷新背景
        refreshBackgroundSafely();

        if(!installConfig(innerConfig())) throw new JsonParseException("配置文件安装失败，请联系客户端制造商！");
        else {
            if (!checkDestroyed()) {
                copyAssets(applicationContext, srcDir + "/version", targetDir + "/version");
                countDownLatch.countDown();
            }
        }
    }

    /**
     * 安全地刷新背景，避免在Activity销毁后操作UI
     */
    private void refreshBackgroundSafely() {
        Activity activity = getActivity();
        View backgroundView = needRefreshBackgroundRef.get();
        
        if (activity != null && backgroundView != null && !activity.isFinishing() && !activity.isDestroyed()) {
            activity.runOnUiThread(() -> {
                // 再次检查Activity状态
                if (!activity.isFinishing() && !activity.isDestroyed() && !isDestroyed) {
                    try {
                        ThemeEngine.getInstance().applyAndSave(
                                activity,
                                backgroundView,
                                LT_BACKGROUND_PATH,
                                DK_BACKGROUND_PATH
                        );
                    } catch (Exception e) {
                        // 静默处理UI更新异常
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private boolean installConfig(@NonNull Config config) {
        if (checkDestroyed()) {
            return false;
        }
        
        ParseAuthlibInjectorServerUtils.parseUrlToConfig(config);
        return ConfigHolder.saveConfig(config);
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    /**
     * 销毁资源，清理引用
     */
    public void destroy() {
        isDestroyed = true;
        activityRef.clear();
        needRefreshBackgroundRef.clear();
    }

    /**
     * 检查是否已被销毁
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }
}
