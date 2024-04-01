package com.tungsten.fcl.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.system.Os;
import com.google.gson.*;
import com.tungsten.fcl.R;
import com.tungsten.fclauncher.FCLauncher;
import com.tungsten.fclauncher.utils.Architecture;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.util.Logging;
import com.tungsten.fclcore.util.Pack200Utils;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fclcore.util.io.IOUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class RuntimeUtils {

    public static boolean isLatest(String targetDir, String srcDir) throws IOException {
        File targetFile = new File(targetDir + "/version");
        return targetFile.exists() && targetDir != null && srcDir != null && Objects.equals(ReadTools.convertToString(RuntimeUtils.class.getResourceAsStream(srcDir + "/version")), ReadTools.readFileTxt(targetDir + "/version"));
    }

    public static boolean isLatest(SharedPreferences sharedPreferences, String key, String defaultValue, String srcDir) throws IOException {
        return sharedPreferences != null && key != null && defaultValue != null && srcDir != null && Objects.equals(ReadTools.convertToString(RuntimeUtils.class.getResourceAsStream(srcDir + "/version")), sharedPreferences.getString(key,defaultValue).trim());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void install(Context context, String targetDir, String srcDir) throws IOException {
        FileUtils.deleteDirectory(new File(targetDir));
        new File(targetDir).mkdirs();
        copyAssets(context, srcDir, targetDir);
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void copyAssets(Context context, String src, String dest) throws IOException {
        String[] fileNames = context.getAssets().list(src);
        if(fileNames.length > 0) {
            File file = new File(dest);
            if (!file.exists())
                file.mkdirs();
            for (String fileName : fileNames) {
                if (!src.equals("")) {
                    copyAssets(context, src + File.separator + fileName, dest + File.separator + fileName);
                } else {
                    copyAssets(context, fileName, dest + File.separator + fileName);
                }
            }
        }else {
            File outFile = new File(dest);
            InputStream is = context.getAssets().open(src);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
        }
    }

    public static void copyAssetsDirToLocalDir(Context context, String assetsPath, String savePath){
        try {
            // 获取assets目录下的所有文件及目录名
            String[] fileNames = context.getAssets().list(assetsPath);
            // 如果是目录
            if (fileNames.length > 0) {
                File file = new File(savePath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssetsDirToLocalDir(context, assetsPath + "/" + fileName, savePath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(assetsPath);
                FileOutputStream fos = new FileOutputStream(savePath);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                // 循环从输入流读取
                while ((byteCount = is.read(buffer)) != -1) {
                    // 将读取的输入流写入到输出流
                    fos.write(buffer, 0, byteCount);
                }
                // 刷新缓冲区
                fos.flush();
                is.close();
                fos.close();
            }
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyAssetsFileToLocalDir(Context context, String assetsFile, String savePath){
        try {
            InputStream is = context.getAssets().open(assetsFile);
            FileOutputStream fos = new FileOutputStream(savePath);
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            // 循环从输入流读取
            while ((byteCount = is.read(buffer)) != -1) {
                // 将读取的输入流写入到输出流
                fos.write(buffer, 0, byteCount);
            }
            // 刷新缓冲区
            fos.flush();
            is.close();
            fos.close();
        }catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void uncompressTarXZ(final InputStream tarFileInputStream, final File dest) throws IOException {
        dest.mkdirs();
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new XZCompressorInputStream(tarFileInputStream));
        TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
        while(tarEntry != null) {
            if(tarEntry.getSize() <= 20480) {
                try {
                    Thread.sleep(25);
                }catch(InterruptedException ignored) {

                }
            }
            File destPath = new File(dest, tarEntry.getName());
            if(tarEntry.isSymbolicLink()) {
                Objects.requireNonNull(destPath.getParentFile()).mkdirs();
                try{
                    Os.symlink(tarEntry.getLinkName().replace("..", dest.getAbsolutePath()), new File(dest, tarEntry.getName()).getAbsolutePath());
                }catch(Throwable e) {
                    Logging.LOG.log(Level.WARNING, e.getMessage());
                }
            }else if(tarEntry.isDirectory()) {
                destPath.mkdirs();
                destPath.setExecutable(true);
            }else if(!destPath.exists() || destPath.length() != tarEntry.getSize()) {
                Objects.requireNonNull(destPath.getParentFile()).mkdirs();
                destPath.createNewFile();
                FileOutputStream os = new FileOutputStream(destPath);
                byte[] buffer = new byte[1024];
                int byteCount;
                while((byteCount = tarIn.read(buffer)) != -1) {
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
        if(!dest.exists()) return;
        String libFolder = FCLauncher.getJreLibDir(javaPath);
        File ftIn = new File(dest, libFolder + "/libfreetype.so.6");
        File ftOut = new File(dest, libFolder + "/libfreetype.so");

        if (ftIn.exists() && (!ftOut.exists() || ftIn.length() != ftOut.length())) ftIn.renameTo(ftOut);

        File fileLib = new File(dest, "/" + libFolder + "/libawt_xawt.so");
        fileLib.delete();
        FileUtils.copyFile(new File(context.getApplicationInfo().nativeLibraryDir, "libawt_xawt.so"), fileLib);
    }

    /**
     * 根据提供的字符串匹配是删文件还是文件夹
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
    **/
    public static boolean delete(String filePath) {
        File file = new File(filePath);
        if(!file.exists()) {
            return false;
        }else {
            if(file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            }else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    /**
     * 删除单个文件
     * @param   filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
    **/
    private static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if(file.isFile() && file.exists()) return file.delete();
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   path 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
    **/
    private static boolean deleteDirectory(String path){
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if(!path.endsWith(File.separator)) path = path + File.separator;

        File dirFile = new File(path);
        if(!dirFile.exists() || !dirFile.isDirectory()) return false;

        flag = true;
        //统计path的根目录下有多少个文件夹和文件总和
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for(File file : files) {
            if(file.isFile()) {
                //删除子文件
                flag = deleteFile(file.getAbsolutePath());
                if(!flag) break;
            }else {
                //删除子目录[递归]
                flag = deleteDirectory(file.getAbsolutePath());
                if(!flag) break;
            }
        }
        if(!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 多线程统计目录大小(适合特别多小文件使用)
     * 该方法仅适合有公有，私有目录
    **/
    public static long getNormalPathSize(String dir) {
        File path = new File(dir);
        //如果path不是目录则输出该文件大小
        if(!path.isDirectory()) {
            return path.length();
        }
        //是目录则开始多线程统计大小
        return IoOperateHolder.FORKJOIN_POOL.invoke(new CalDirCommand(path));
    }
    static class CalDirCommand extends RecursiveTask<Long> {
        private File folder;
        CalDirCommand(File folder){
            this.folder = folder;
        }
        @Override
        protected Long compute() {
            AtomicLong size = new AtomicLong(0);
            File[] files = folder.listFiles();
            if(files == null || files.length == 0) {
                return 0L;
            }
            List<ForkJoinTask<Long>> jobs = new ArrayList<>();
            for(File f : files) {
                if(!f.isDirectory()) size.addAndGet(f.length());
                else jobs.add(new CalDirCommand(f));
            }
            for(ForkJoinTask<Long> t : invokeAll(jobs)) size.addAndGet(t.join());
            return size.get();
        }
    }
    private static final class IoOperateHolder {
        final static ForkJoinPool FORKJOIN_POOL = new ForkJoinPool();
    }

    public static void copyFile(String srcPath,String destPath){
        File src = new File(srcPath);
        File dest = new File(destPath);

        try {
            InputStream inputStream = new BufferedInputStream(Files.newInputStream(src.toPath()));
            OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(dest.toPath()));
            byte[] flush = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(flush)) != -1) outputStream.write(flush,0,len);

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadConfiguration(Context context) {
        // 先删除旧文件
        RuntimeUtils.delete(FCLPath.FILES_DIR + "/menu_setting.json");
        RuntimeUtils.delete(FCLPath.FILES_DIR + "/config.json");
        RuntimeUtils.delete(FCLPath.FILES_DIR + "/global_config.json");

        // 删除launcher.xml需要更新字段
        SharedPreferences.Editor editor = context.getSharedPreferences("launcher", MODE_PRIVATE).edit();
        editor.remove("this_game_resources_directory");
        editor.apply();

        try {
            // 读取config.json配置文件并解析
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(ReadTools.getAssetReader(context, "others/config.json"), JsonObject.class);
            JsonObject configurations = jsonObject.getAsJsonObject("configurations");

            // 若不存在gameDir属性值有问题则添加一个
            for(String key : configurations.keySet()) {
                JsonObject config = configurations.getAsJsonObject(key);

                if(!config.has("gameDir") || !isPath(config.get("gameDir").getAsString()) || !new File(config.get("gameDir").getAsString()).canWrite() || !new File(config.get("gameDir").getAsString()).canRead()) {
                    if(key.equals(context.getString(R.string.profile_private))) config.addProperty("gameDir", FCLPath.PRIVATE_COMMON_DIR);
                    else config.addProperty("gameDir", FCLPath.SHARED_COMMON_DIR);
                }

            }

            // 将修改后的 "configurations" 字段与原始 JSON 字符串中的其他字段合并
            JsonObject mergedJsonObject = new JsonObject();
            for (String key : jsonObject.keySet()) {
                if (!key.equals("configurations")) {
                    mergedJsonObject.add(key, jsonObject.get(key));
                } else {
                    mergedJsonObject.add("configurations", jsonObject.getAsJsonObject("configurations"));
                }
            }


            // 重新写入新文件
            RuntimeUtils.writeStringToFile(FCLPath.FILES_DIR, "config.json", gson.toJson(mergedJsonObject));
            RuntimeUtils.copyAssets(context, "others/menu_setting.json", FCLPath.FILES_DIR + "/menu_setting.json");
        }catch (JsonSyntaxException | JsonIOException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPath(String str) {
        try {
            Paths.get(str);
            return true;
        }catch(InvalidPathException e) {
            return false;
        }
    }

    public static void writeStringToFile(String path,String fileName, String content) throws IOException {
        File file = new File(path, fileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(content);
        writer.close();
    }

    public static String getApplicationThisGameDirectory(final Context context) {
        SharedPreferences launcher = context.getSharedPreferences("launcher", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = launcher.edit();

        String launcherFileThisGameDirectory = launcher.getString("this_game_resources_directory", null);
        if(launcherFileThisGameDirectory != null && isPath(launcherFileThisGameDirectory)) return launcherFileThisGameDirectory;

        JsonObject configJsonObject;

        try {
            if(new File(FCLPath.FILES_DIR + "/config.json").exists()) {
                Gson gson = new Gson();
                configJsonObject = gson.fromJson(ReadTools.convertToString(Files.newInputStream(Paths.get(FCLPath.FILES_DIR + "/config.json"))), JsonObject.class);
            }else {
                Gson gson = new Gson();
                configJsonObject = gson.fromJson(ReadTools.getAssetReader(context, "others/config.json"), JsonObject.class);
            }
        }catch(JsonSyntaxException | JsonIOException | IOException e) {
            edit.putString("this_game_resources_directory", FCLPath.SHARED_COMMON_DIR);
            edit.apply();
            return FCLPath.SHARED_COMMON_DIR;
        }

        JsonElement lastElement = configJsonObject.get("last");
        String lastValue = lastElement != null && !lastElement.isJsonNull() ? lastElement.getAsString() : null;
        String s = lastValue != null && lastValue.equals(context.getString(R.string.profile_private)) ? FCLPath.PRIVATE_COMMON_DIR : FCLPath.SHARED_COMMON_DIR;
        edit.putString("this_game_resources_directory", s);
        edit.apply();

        return s;
    }
}
