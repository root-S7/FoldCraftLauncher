package com.tungsten.fcl.util;

import static com.tungsten.fcl.setting.Config.fromJson;
import static com.tungsten.fcl.setting.ConfigHolder.*;
import static com.tungsten.fcl.util.AndroidUtils.*;
import static com.tungsten.fcl.util.RuntimeUtils.*;
import static com.tungsten.fclauncher.utils.AssetsPath.LAUNCHER_CONFIG;
import static com.tungsten.fclauncher.utils.FCLPath.*;
import static com.tungsten.fclcore.util.io.FileUtils.*;
import static com.tungsten.fclcore.util.io.IOUtils.readFullyAsString;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonParseException;
import com.tungsten.fcl.setting.Config;
import com.tungsten.fcl.util.check.FileFormat;

import java.io.*;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class InstallResources {
    private final CountDownLatch countDownLatch;
    private final Set<FileInfo> checkFiles;
    private volatile boolean configSuccess = false;
    private final Config innerConfig;

    public InstallResources(Context context) {
        this.checkFiles = new FileFormat().getCheckFiles();
        this.countDownLatch = new CountDownLatch(checkFiles.size() + 1);
        this.innerConfig = safeLoadConfig(context);
    }

    private Config safeLoadConfig(Context context) {
        try {
            Config parsed = fromJson(readFullyAsString(openAssets(context, LAUNCHER_CONFIG)));
            return validateProfile(parsed);
        }catch(Exception ignored) {}
        return validateProfile(new Config());
    }

    public void installGameFiles(String oldInstallDir, String srcDir, final SharedPreferences.Editor editor, Context context) throws IOException, InterruptedException {
        forceDelete(LOG_DIR, CONTROLLER_DIR, oldInstallDir);

        if(!countDownLatch.await(44, TimeUnit.SECONDS) || !configSuccess) throw new InterruptedException("配置文件安装失败，无法继续安装游戏文件");
        install(context, getSelectedPath(innerConfig).getAbsolutePath(), srcDir);
        if(editor != null) {
            editor.putBoolean("isFirstInstall", false);
            editor.apply();
        }
    }

    public void installConfigFiles(Context context) throws Exception {
        try {
            batchDelete(new File(FILES_DIR), new File(CONFIG_DIR), context.getCacheDir(), context.getCodeCacheDir());

            for(FileInfo file : checkFiles) {
                try {
                    copyAssets(context, file.getAssPath(), file.getOutPath());
                    countDownLatch.countDown();
                }catch(FileNotFoundException e) {
                    throw new FileNotFoundException("未能在APK的assets目录中找到该文件“" + file.getAssPath() + "”");
                }catch(IOException e) {
                    throw new IOException("尝试读取/写入文件时发生致命错误：" + e);
                }catch(Exception e) {
                    throw new Exception("未知错误：" + e);
                }
            }

            installConfig();
            configSuccess = true;
            countDownLatch.countDown();
        }catch(Exception e) {
            while(countDownLatch.getCount() > 0) countDownLatch.countDown();
            throw e;
        }
    }

    protected void installConfig() {
        ParseAuthlibInjectorServerUtils.parseUrlToConfig(innerConfig);

        try {
            writeToConfig(innerConfig);
        }catch(Exception ignored) {
            throw new JsonParseException("配置文件安装失败，请联系客户端制造商！");
        }
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public boolean isSuccess() {
        return configSuccess;
    }
}
