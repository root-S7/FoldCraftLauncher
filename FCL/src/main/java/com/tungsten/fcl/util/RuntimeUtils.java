package com.tungsten.fcl.util;

import static com.tungsten.fclauncher.utils.FCLPath.*;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.system.Os;
import android.view.View;

import com.tungsten.fcl.R;
import com.tungsten.fcl.setting.Config;
import com.tungsten.fclauncher.FCLauncher;
import com.tungsten.fclauncher.utils.Architecture;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.util.Logging;
import com.tungsten.fclcore.util.Pack200Utils;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fclcore.util.io.IOUtils;
import com.tungsten.fclcore.util.io.Unzipper;
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog;
import com.tungsten.fcllibrary.component.theme.ThemeEngine;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class RuntimeUtils {

    public static boolean isLatest(String targetDir, String srcDir) throws IOException {
        File targetFile = new File(targetDir + "/version");
        String version = IOUtils.readFullyAsString(RuntimeUtils.class.getResourceAsStream(srcDir + "/version"));
        return targetFile.exists() && FileUtils.readText(targetFile).equals(version);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void install(Context context, String targetDir, String srcDir) throws IOException {
        FileUtils.deleteDirectory(new File(targetDir));
        new File(targetDir).mkdirs();
        copyAssets(context, srcDir, targetDir);
    }

    private final static InstallResources installResources = new InstallResources();
    public static void installGameFiles(Activity activity, String oldInstallDir, String srcDir,final SharedPreferences.Editor editor) throws IOException, ExecutionException, InterruptedException {
        installResources.installGameFiles(activity, oldInstallDir, srcDir);

        if(editor != null) {
            editor.putBoolean("isFirstInstall", false);
            editor.apply();
        }
    }

    public static void installConfigFiles(Activity activity,View view , String targetDir, String srcDir) throws IOException {
        installResources.installConfigFiles(activity, view, targetDir, srcDir);
    }

    protected static class InstallResources {
        private final CountDownLatch countDownLatch;

        public InstallResources() {
            this.countDownLatch = new CountDownLatch(CheckFileFormat.defaultCheckFiles.size() + 1);
        }

        public void installGameFiles(Activity activity, String oldInstallDir, String srcDir) throws IOException, ExecutionException, InterruptedException {
            FileUtils.deleteDirectory(new File(FCLPath.SHARED_COMMON_DIR)); // 先删除默认目录中的按键和日志内容

            FileUtils.deleteDirectory(new File(oldInstallDir)); // 如果config.json文件修改后则删除旧的config.json文件中目录资源

            countDownLatch.await(); // 等待配置文件线程关键文件操作完毕后才能继续往下操作

            install(activity, ConfigUtils.getGameDirectory(), srcDir); // 安装游戏资源
        }

        public void installConfigFiles(Activity activity, View needRefreshBackground, String targetDir, String srcDir) throws IOException {
            FileUtils.batchDelete(new File(FILES_DIR), new File(CONFIG_DIR), activity.getCacheDir(), activity.getCodeCacheDir());

            Set<CheckFileFormat.FileInfo> defaultCheckFiles = CheckFileFormat.defaultCheckFiles;
            for(CheckFileFormat.FileInfo file : defaultCheckFiles) {
                Path externalPath = file.getExternalPath();
                try {
                    copyAssets(activity, file.getInternalPath(), externalPath == null ? null : externalPath.toString());
                    countDownLatch.countDown(); // CountDownLatch计数器为0时，调用await()的线程不会阻塞
                } catch (FileNotFoundException e) {
                    enableAlertDialog(activity, countDownLatch, "未能在APK的assets目录中找到该文件“" + file.getInternalPath() + "”");
                } catch (IOException e) {
                    enableAlertDialog(activity, countDownLatch, "尝试读取/写入文件时发生致命错误：" + e);
                } catch (Exception e) {
                    enableAlertDialog(activity, countDownLatch, "未知错误：" + e);
                }
            }
            if(needRefreshBackground != null) {
                activity.runOnUiThread(() -> ThemeEngine.getInstance().applyAndSave(
                        activity,
                        needRefreshBackground,
                        FCLPath.LT_BACKGROUND_PATH,
                        FCLPath.DK_BACKGROUND_PATH
                ));
            }
            ParseAuthlibInjectorServerUtils.parseUrlAndWriteToFile(ConfigUtils.getNoProblemConfig(true, new Config()));
            RuntimeUtils.copyAssets(activity, srcDir + "/version", targetDir + "/version");
            countDownLatch.countDown();
        }

        private void enableAlertDialog(Activity activity, final CountDownLatch latch, String message) {
            activity.runOnUiThread(() -> new FCLAlertDialog.Builder(activity)
                    .setTitle("警告")
                    .setMessage(message)
                    .setPositiveButton("确定", latch::countDown)
                    .setCancelable(false)
                    .create()
                    .show());
        }
    }

    public static void installJna(Context context, String targetDir, String srcDir) throws IOException {
        FileUtils.deleteDirectory(new File(targetDir));
        new File(targetDir).mkdirs();
        copyAssets(context, srcDir, targetDir);
        File file = new File(FCLPath.JNA_PATH, "jna-arm64.zip");
        new Unzipper(file, new File(FCLPath.RUNTIME_DIR)).unzip();
        file.delete();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void installJava(Context context, String targetDir, String srcDir) throws IOException {
        FileUtils.deleteDirectory(new File(targetDir));
        new File(targetDir).mkdirs();
        String universalPath = srcDir + "/universal.tar.xz";
        String archPath = srcDir + "/bin-" + Architecture.archAsString(Architecture.getDeviceArchitecture()) + ".tar.xz";
        String version = IOUtils.readFullyAsString(RuntimeUtils.class.getResourceAsStream("/assets/" + srcDir + "/version"));
        uncompressTarXZ(context.getAssets().open(universalPath), new File(targetDir));
        uncompressTarXZ(context.getAssets().open(archPath), new File(targetDir));
        FileUtils.writeText(new File(targetDir + "/version"), version);
        patchJava(context, targetDir);
    }

    public static void copyAssets(Context context, String src, String dest) throws IOException {
        if(context == null || src == null || dest == null) return;

        // 获取指定路径下的文件或目录列表
        String[] fileNames = context.getAssets().list(src);

        if (fileNames != null && fileNames.length > 0) { // 当前路径为目录
            File destDir = new File(dest);

            // 确保目标目录存在
            if (!destDir.exists() && !destDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + dest);
            }

            // 遍历目录下的文件/子目录
            for (String fileName : fileNames) {
                String newSrc = src.isEmpty() ? fileName : src + File.separator + fileName;
                String newDest = dest + File.separator + fileName;

                // 递归复制
                copyAssets(context, newSrc, newDest);
            }
        } else { // 当前路径为文件
            File outFile = new File(dest);

            // 确保父目录存在
            File parentDir = outFile.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create parent directory: " + parentDir.getAbsolutePath());
            }

            // 使用 try-with-resources 确保资源关闭
            try (InputStream is = context.getAssets().open(src);
                 FileOutputStream fos = new FileOutputStream(outFile)) {

                // 以缓冲区形式复制文件
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }

                // 确保数据刷新到文件
                fos.flush();
            }
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void uncompressTarXZ(final InputStream tarFileInputStream, final File dest) throws IOException {
        dest.mkdirs();
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new XZCompressorInputStream(tarFileInputStream));
        TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
        while (tarEntry != null) {
            if (tarEntry.getSize() <= 20480) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ignored) {

                }
            }
            File destPath = new File(dest, tarEntry.getName());
            if (tarEntry.isSymbolicLink()) {
                Objects.requireNonNull(destPath.getParentFile()).mkdirs();
                try {
                    Os.symlink(tarEntry.getLinkName().replace("..", dest.getAbsolutePath()), new File(dest, tarEntry.getName()).getAbsolutePath());
                } catch (Throwable e) {
                    Logging.LOG.log(Level.WARNING, e.getMessage());
                }
            } else if (tarEntry.isDirectory()) {
                destPath.mkdirs();
                destPath.setExecutable(true);
            } else if (!destPath.exists() || destPath.length() != tarEntry.getSize()) {
                Objects.requireNonNull(destPath.getParentFile()).mkdirs();
                destPath.createNewFile();
                FileOutputStream os = new FileOutputStream(destPath);
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = tarIn.read(buffer)) != -1) {
                    os.write(buffer, 0, byteCount);
                }
                os.close();
            }
            tarEntry = tarIn.getNextTarEntry();
        }
        tarIn.close();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void patchJava(Context context, String javaPath) throws IOException {
        Pack200Utils.unpack(context.getApplicationInfo().nativeLibraryDir, javaPath);
        File dest = new File(javaPath);
        if(!dest.exists())
            return;
        String libFolder = FCLauncher.getJreLibDir(javaPath);
        File ftIn = new File(dest, libFolder + "/libfreetype.so.6");
        File ftOut = new File(dest, libFolder + "/libfreetype.so");
        if (ftIn.exists() && (!ftOut.exists() || ftIn.length() != ftOut.length())) {
            ftIn.renameTo(ftOut);
        }
        File fileLib = new File(dest, "/" + libFolder + "/libawt_xawt.so");
        fileLib.delete();
        FileUtils.copyFile(new File(context.getApplicationInfo().nativeLibraryDir, "libawt_xawt.so"), fileLib);
    }

}
