package com.tungsten.fcl.util;

import static com.tungsten.fcl.setting.ConfigHolder.getSelectedPath;
import static com.tungsten.fcl.setting.ConfigHolder.innerConfig;
import static com.tungsten.fcl.util.RuntimeUtils.copyAssets;
import static com.tungsten.fcl.util.RuntimeUtils.install;
import static com.tungsten.fclauncher.utils.FCLPath.*;
import static com.tungsten.fclcore.util.io.FileUtils.batchDelete;
import static com.tungsten.fclcore.util.io.FileUtils.forceDelete;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;
import com.tungsten.fcl.setting.Config;
import com.tungsten.fcl.setting.ConfigHolder;
import com.tungsten.fcllibrary.component.theme.ThemeEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class InstallResources {
    private final CountDownLatch countDownLatch;
    private final Set<CheckFileFormat.FileInfo<?>> checkFiles;
    private final Activity thisActivity;
    private final View needRefreshBackground;

    public InstallResources(Activity activity, View needRefreshBackground) {
        if(activity == null) throw new NullPointerException("错误，无效的活动页面。请将该问题反馈给原始制造商！");
        if(needRefreshBackground != null && activity.findViewById(needRefreshBackground.getId()) == null) throw new IllegalArgumentException("在“" + activity.getClass().getName() + "”视图未找到名叫“" + needRefreshBackground.getClass().getName() + "”的组件");

        this.thisActivity = activity;
        this.needRefreshBackground = needRefreshBackground;
        this.checkFiles = new CheckFileFormat(activity.getApplicationContext()).getDefaultCheckFiles();
        this.countDownLatch = new CountDownLatch(checkFiles.size() + 1);
    }

    public void installGameFiles(String oldInstallDir, String srcDir, final SharedPreferences.Editor editor) throws IOException, InterruptedException {
        forceDelete(LOG_DIR, CONTROLLER_DIR, oldInstallDir); // 先删除默认目录中的按键和日志内容，如果config.json文件修改后则删除旧的config.json文件中目录资源

        countDownLatch.await(); // 等待配置文件线程关键文件操作完毕后才能继续往下操作
        install(thisActivity.getApplicationContext(), getSelectedPath(innerConfig()).getAbsolutePath(), srcDir); // 安装游戏资源
        if(editor != null) {
            editor.putBoolean("isFirstInstall", false);
            editor.apply();
        }
    }

    public void installConfigFiles(String targetDir, String srcDir) throws Exception {
        batchDelete(new File(FILES_DIR), new File(CONFIG_DIR), thisActivity.getCacheDir(), thisActivity.getCodeCacheDir());

        for(CheckFileFormat.FileInfo<?> file : checkFiles) {
            Path externalPath = file.getExternalPath();
            try {
                copyAssets(thisActivity.getApplicationContext(), file.getInternalPath(), externalPath == null ? null : externalPath.toString());
                countDownLatch.countDown(); // CountDownLatch计数器为0时，调用await()的线程不会阻塞
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("未能在APK的assets目录中找到该文件“" + file.getInternalPath() + "”");
            } catch (IOException e) {
                throw new IOException("尝试读取/写入文件时发生致命错误：" + e);
            } catch (Exception e) {
                throw new Exception("未知错误：" + e);
            }
        }

        if(needRefreshBackground != null) {
            thisActivity.runOnUiThread(() -> ThemeEngine.getInstance().applyAndSave(
                    thisActivity,
                    needRefreshBackground,
                    LT_BACKGROUND_PATH,
                    DK_BACKGROUND_PATH
            ));
        }

        if(!installConfig(innerConfig())) throw new JsonParseException("配置文件安装失败，请联系客户端制造商！");
        else {
            copyAssets(thisActivity.getApplicationContext(), srcDir + "/version", targetDir + "/version");
            countDownLatch.countDown();
        }
    }

    private boolean installConfig(@NonNull Config config) {
        ParseAuthlibInjectorServerUtils.parseUrlToConfig(config);

        return ConfigHolder.saveConfig(config);
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }
}
