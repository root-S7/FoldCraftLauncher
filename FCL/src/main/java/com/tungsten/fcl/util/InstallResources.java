package com.tungsten.fcl.util;

import static com.tungsten.fcl.setting.ConfigHolder.*;
import static com.tungsten.fcl.util.RuntimeUtils.copyAssets;
import static com.tungsten.fcl.util.RuntimeUtils.install;
import static com.tungsten.fclauncher.utils.FCLPath.*;
import static com.tungsten.fclcore.util.io.FileUtils.*;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;
import com.tungsten.fcl.setting.Config;
import com.tungsten.fcl.setting.ConfigHolder;

import java.io.*;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class InstallResources {
    private final CountDownLatch countDownLatch;
    private final Set<CheckFileFormat.FileInfo<?>> checkFiles;
    private volatile boolean configSuccess = false;

    public InstallResources(Context context) {
        this.checkFiles = new CheckFileFormat(context.getApplicationContext()).getDefaultFiles();
        this.countDownLatch = new CountDownLatch(checkFiles.size() + 1);
    }

    public void installGameFiles(String oldInstallDir, String srcDir, final SharedPreferences.Editor editor, Context context) throws IOException, InterruptedException {
        forceDelete(LOG_DIR, CONTROLLER_DIR, oldInstallDir);

        if(!countDownLatch.await(44, TimeUnit.SECONDS) || !configSuccess) throw new InterruptedException("配置文件安装失败，无法继续安装游戏文件");
        install(context, getSelectedPath(innerConfig()).getAbsolutePath(), srcDir);
        if(editor != null) {
            editor.putBoolean("isFirstInstall", false);
            editor.apply();
        }
    }

    public void installConfigFiles(String targetDir, String srcDir, Context context) throws Exception {
        try {
            batchDelete(new File(FILES_DIR), new File(CONFIG_DIR), context.getCacheDir(), context.getCodeCacheDir());

            for(CheckFileFormat.FileInfo<?> file : checkFiles) {
                Path externalPath = file.getExternalPath();
                try {
                    copyAssets(context, file.getInternalPath(), externalPath == null ? null : externalPath.toString());
                    countDownLatch.countDown();
                }catch(FileNotFoundException e) {
                    throw new FileNotFoundException("未能在APK的assets目录中找到该文件“" + file.getInternalPath() + "”");
                }catch(IOException e) {
                    throw new IOException("尝试读取/写入文件时发生致命错误：" + e);
                }catch(Exception e) {
                    throw new Exception("未知错误：" + e);
                }
            }

            if(!installConfig(innerConfig())) throw new JsonParseException("配置文件安装失败，请联系客户端制造商！");
            else {
                copyAssets(context, srcDir + "/version", targetDir + "/version");
                configSuccess = true;
                countDownLatch.countDown();
            }
        }catch(Exception e) {
            while(countDownLatch.getCount() > 0) countDownLatch.countDown();
            throw e;
        }
    }

    private boolean installConfig(@NonNull Config config) {
        ParseAuthlibInjectorServerUtils.parseUrlToConfig(config);

        return ConfigHolder.saveConfig(config);
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public boolean isSuccess() {
        return configSuccess;
    }
}
