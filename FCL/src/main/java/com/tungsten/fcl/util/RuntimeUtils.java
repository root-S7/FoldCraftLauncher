package com.tungsten.fcl.util;

import static com.tungsten.fclcore.util.io.FileUtils.forceDeleteQuietly;
import static com.tungsten.fclcore.util.io.FileUtils.writeText;

import android.content.Context;
import android.system.Os;

import com.tungsten.fclauncher.FCLauncher;
import com.tungsten.fclauncher.utils.Architecture;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.util.Logging;
import com.tungsten.fclcore.util.Pack200Utils;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fclcore.util.io.IOUtils;
import com.tungsten.fclcore.util.io.Unzipper;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Level;

public class RuntimeUtils {

    public static boolean isLatest(String targetDir, String srcDir) {
        try {
            File targetFile = new File(targetDir + "/version");
            String version = IOUtils.readFullyAsString(RuntimeUtils.class.getResourceAsStream(srcDir + "/version"));
            return targetFile.exists() && FileUtils.readText(targetFile).equals(version);
        }catch(Exception e) {
            return true;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void install(Context context, String targetDir, String srcDir) throws IOException {
        forceDeleteQuietly(new File(targetDir));
        new File(targetDir).mkdirs();
        copyAssets(context, srcDir, targetDir);
    }

    public static void installJna(Context context, String targetDir, String srcDir) throws IOException {
        forceDeleteQuietly(new File(targetDir));
        new File(targetDir).mkdirs();
        copyAssets(context, srcDir, targetDir);
        File file = new File(FCLPath.JNA_PATH, "jna-arm64.zip");
        new Unzipper(file, new File(FCLPath.RUNTIME_DIR)).unzip();
        file.delete();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void installJava(Context context, String targetDir, String srcDir) throws IOException {
        forceDeleteQuietly(new File(targetDir));
        new File(targetDir).mkdirs();
        String universalPath = srcDir + "/universal.tar.xz";
        String archPath = srcDir + "/bin-" + Architecture.archAsString(Architecture.getDeviceArchitecture()) + ".tar.xz";
        String version = IOUtils.readFullyAsString(RuntimeUtils.class.getResourceAsStream("/assets/" + srcDir + "/version"));
        uncompressTarXZ(context.getAssets().open(universalPath), new File(targetDir));
        uncompressTarXZ(context.getAssets().open(archPath), new File(targetDir));
        writeText(new File(targetDir + "/version"), version);
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
        if (!dest.exists())
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
