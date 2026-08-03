package com.tungsten.fcl.util;

import static com.tungsten.fclcore.util.io.FileUtils.forceDeleteQuietly;
import static com.tungsten.fclcore.util.io.FileUtils.writeText;
import static com.tungsten.fclcore.util.io.IOUtils.DEFAULT_BUFFER_SIZE;
import static com.tungsten.fcllibrary.util.ConvertUtils.*;

import android.content.Context;
import android.content.res.AssetManager;
import android.system.Os;
import android.util.Log;

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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.logging.Level;

public class RuntimeUtils {

    public static boolean isLatest(String targetDir, String srcDir) throws IOException {
        File targetFile = new File(targetDir + "/version");

        try(InputStream stream = RuntimeUtils.class.getResourceAsStream(srcDir + "/version")) {
            if(stream == null) return true;

            String assetsStr = IOUtils.readFullyAsString(stream).trim();
            long assetsVersion = stringToLong(assetsStr, -1);
            if(!targetFile.exists()) return false;

            String installedStr = FileUtils.readText(targetFile).trim();
            if(installedStr.isEmpty()) return false;
            long installedVersion = stringToLong(installedStr, -1);

            return assetsVersion == installedVersion;
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
        copyAssets(context.getAssets(), src, new File(dest));
    }

    private static void copyAssets(AssetManager am, String src, File dest) throws IOException {
        String[] files = am.list(src);
        if(files != null && files.length > 0) {
            if(!dest.exists() && !dest.mkdirs()) throw new IOException("无法创建目录: " + dest);
            for(String file : files) copyAssets(am, src.isEmpty() ? file : src + "/" + file, new File(dest, file));

            return;
        }


        File parent = dest.getParentFile();
        if(parent != null && !parent.exists() && !parent.mkdirs()) throw new IOException("无法创建目录: " + parent);
        try(BufferedInputStream bis = new BufferedInputStream(am.open(src), DEFAULT_BUFFER_SIZE);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest), DEFAULT_BUFFER_SIZE)) {

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int len;
            while((len = bis.read(buffer)) != -1) bos.write(buffer, 0, len);
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
        String libFolder = FCLauncher.getJavaLibDir(javaPath);
        if (FCLauncher.isJDK8(javaPath)) {
            libFolder = "/jre" + libFolder;
        }
        File ftIn = new File(dest, libFolder + "/libfreetype.so.6");
        File ftOut = new File(dest, libFolder + "/libfreetype.so");
        if (ftIn.exists() && (!ftOut.exists() || ftIn.length() != ftOut.length())) {
            ftIn.renameTo(ftOut);
        }
        ftIn = new File(dest, FCLauncher.getJavaLibDir(javaPath) + "/libfreetype.so");
        if (FCLauncher.isJDK8(javaPath) && ftIn.exists()) {
            ftIn.renameTo(ftOut);
        }
        File fileLib = new File(dest, libFolder + "/libawt_xawt.so");
        fileLib.delete();
        FileUtils.copyFile(new File(context.getApplicationInfo().nativeLibraryDir, "libawt_xawt.so"), fileLib);
    }

}
