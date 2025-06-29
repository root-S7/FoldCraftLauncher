package com.tungsten.fcl.util;

import static com.tungsten.fcl.setting.ConfigHolder.getSelectedPath;
import static com.tungsten.fcl.setting.ConfigHolder.innerConfig;
import static com.tungsten.fcl.util.RuntimeUtils.copyAssets;
import static com.tungsten.fcl.util.RuntimeUtils.install;
import static com.tungsten.fclauncher.utils.FCLPath.CONFIG_DIR;
import static com.tungsten.fclauncher.utils.FCLPath.FILES_DIR;
import static com.tungsten.fclcore.util.io.FileUtils.batchDelete;
import static com.tungsten.fclcore.util.io.FileUtils.forceDelete;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;

import com.tungsten.fcl.setting.Config;
import com.tungsten.fcl.setting.ConfigHolder;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog;
import com.tungsten.fcllibrary.component.theme.ThemeEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class InstallResources {
    private final CountDownLatch countDownLatch;
    private final CheckFileFormat checkFileFormat;
    private final Activity thisActivity;
    private View needRefreshBackground;

    public InstallResources(Activity activity, View needRefreshBackground) {
        if(activity == null) throw new NullPointerException("错误，无效的活动页面。请将该问题反馈给原始制造商！");
        if(needRefreshBackground != null && activity.findViewById(needRefreshBackground.getId()) == null) throw new IllegalArgumentException("在“" + activity.getClass().getName() + "”视图未找到名叫“" + needRefreshBackground.getClass().getName() + "”的组件");

        this.thisActivity = activity;
        this.needRefreshBackground = needRefreshBackground;
        this.checkFileFormat = new CheckFileFormat(activity);
        this.countDownLatch = new CountDownLatch(checkFileFormat.getDefaultCheckFiles().size() + 1);
    }

    public void installGameFiles(String oldInstallDir, String srcDir, final SharedPreferences.Editor editor) throws IOException, InterruptedException {
        forceDelete(FCLPath.LOG_DIR, FCLPath.CONTROLLER_DIR, oldInstallDir); // 先删除默认目录中的按键和日志内容，如果config.json文件修改后则删除旧的config.json文件中目录资源

        countDownLatch.await(); // 等待配置文件线程关键文件操作完毕后才能继续往下操作
        install(thisActivity, getSelectedPath(innerConfig()).getAbsolutePath(), srcDir); // 安装游戏资源
        if(editor != null) {
            editor.putBoolean("isFirstInstall", false);
            editor.apply();
        }
    }

    public void installConfigFiles(String targetDir, String srcDir) throws IOException {
        batchDelete(new File(FILES_DIR), new File(CONFIG_DIR), thisActivity.getCacheDir(), thisActivity.getCodeCacheDir());

        Set<CheckFileFormat.FileInfo<?>> defaultCheckFiles = checkFileFormat.getDefaultCheckFiles();
        for(CheckFileFormat.FileInfo<?> file : defaultCheckFiles) {
            Path externalPath = file.getExternalPath();
            try {
                copyAssets(thisActivity, file.getInternalPath(), externalPath == null ? null : externalPath.toString());
                countDownLatch.countDown(); // CountDownLatch计数器为0时，调用await()的线程不会阻塞
            } catch (FileNotFoundException e) {
                enableAlertDialog(thisActivity, "未能在APK的assets目录中找到该文件“" + file.getInternalPath() + "”");
                break;
            } catch (IOException e) {
                enableAlertDialog(thisActivity, "尝试读取/写入文件时发生致命错误：" + e);
                break;
            } catch (Exception e) {
                enableAlertDialog(thisActivity, "未知错误：" + e);
                break;
            }
        }

        if(needRefreshBackground != null) {
            thisActivity.runOnUiThread(() -> ThemeEngine.getInstance().applyAndSave(
                    thisActivity,
                    needRefreshBackground,
                    FCLPath.LT_BACKGROUND_PATH,
                    FCLPath.DK_BACKGROUND_PATH
            ));
        }
        if(!installConfig(innerConfig())) {
            enableAlertDialog(thisActivity, "配置文件安装失败，请联系客户端制造商！");
            return;
        }
        copyAssets(thisActivity, srcDir + "/version", targetDir + "/version");

        countDownLatch.countDown();
    }

    private boolean installConfig(@NonNull Config config) {
        ParseAuthlibInjectorServerUtils.parseUrlAndWriteToFile(config);

        return ConfigHolder.saveConfig(config);
    }

    private void enableAlertDialog(Activity activity, String message) {
        activity.runOnUiThread(() -> new FCLAlertDialog.Builder(activity)
                .setTitle("警告")
                .setMessage(message + "\n由于该错误是致命性的，点击“确定”按钮后将关闭应用")
                .setPositiveButton("确定", () -> System.exit(-1))
                .setCancelable(false)
                .create()
                .show());
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public CheckFileFormat getCheckFileFormat() {
        return checkFileFormat;
    }

    public void setNeedRefreshBackground(View needRefreshBackground) {
        if(!checkView(thisActivity, needRefreshBackground)) throw new IllegalArgumentException("错误，无效的活动页面。请将该问题反馈给原始制造商！");

        this.needRefreshBackground = needRefreshBackground;
    }

    /**
     * 传入的“activity”种是否存在该“view”
     *
     * @param activity 当前页面
     * @param view 当前页面哪个组件（需要绑定id）
     * @return 返回是否包含
     **/
    protected static boolean checkView(Activity activity, View view) {
        try {
            return activity != null && view != null && activity.findViewById(view.getId()) != null;
        }catch(Exception e) {
            return false;
        }
    }
}
