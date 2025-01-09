package com.tungsten.fcl.util;

import android.app.Activity;
import android.graphics.Bitmap;
import com.mio.util.ImageUtil;
import com.tungsten.fcl.setting.ConfigHolder;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fclcore.util.io.IOUtils;
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog;
import com.tungsten.fcllibrary.component.dialog.FCLWaitDialog;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckFileFormat {

    /**
     * 检查文件格式是否正确
    **/
    protected final Set<FileInfo> defaultCheckFiles;
    protected Set<String> extraNeedCheckInternalFile = new HashSet<>();
    protected final Activity activity;

    public CheckFileFormat(Activity activity, String... extraNeedFile) {
        if(extraNeedFile != null && (extraNeedFile.length > 0)) {
            Arrays.stream(extraNeedFile)
                    .filter(file -> file != null && !file.isEmpty())  // 排除 null 和 空字符串
                    .forEach(extraNeedCheckInternalFile::add);  // 添加到集合中
        }

        FCLPath.loadPaths(activity);

        this.activity = activity;
        defaultCheckFiles = Set.of(
                new FileInfo(FCLPath.ASSETS_CONFIG_JSON, ConfigHolder.CONFIG_PATH),
                new FileInfo(FCLPath.ASSETS_MENU_SETTING_JSON, Paths.get(FCLPath.FILES_DIR + "/menu_setting.json")),
                new FileInfo(FCLPath.ASSETS_AUTH_INJECTOR_SERVER_JSON, null),
                new FileInfo(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/lt.png", Paths.get(FCLPath.LT_BACKGROUND_PATH)),
                new FileInfo(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/dk.png", Paths.get(FCLPath.DK_BACKGROUND_PATH)),
                new FileInfo(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/cursor.png", Paths.get(FCLPath.FILES_DIR + "/cursor.png")),
                new FileInfo(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/menu_icon.png", Paths.get(FCLPath.FILES_DIR + "/menu_icon.png"))
        );
    }

    public Set<FileInfo> getDefaultCheckFiles() {
        return defaultCheckFiles;
    }

    public Set<String> getExtraNeedCheckInternalFile() {
        return extraNeedCheckInternalFile;
    }

    public void setExtraNeedCheckInternalFile(Set<String> extraNeedCheckInternalFile) {
        this.extraNeedCheckInternalFile = extraNeedCheckInternalFile;
    }

    protected Set<String> transformDataStructure(boolean needCheckExtraFiles) {
        Set<String> collect = defaultCheckFiles.stream().filter(
                fileInfo -> fileInfo != null && fileInfo.getInternalPath() != null && !fileInfo.getInternalPath().isEmpty()
        ).map(FileInfo::getInternalPath).collect(Collectors.toSet());

        if(needCheckExtraFiles) collect.addAll(extraNeedCheckInternalFile);

        return collect;
    }

    public LinkedHashMap<String, FileType> getFileExtension(boolean needCheckExtraFiles) {
        LinkedHashMap<String, FileType> fileExtension = new LinkedHashMap<>();
        Set<String> internalNeedCheckFiles = transformDataStructure(needCheckExtraFiles);

        for(String fileType : internalNeedCheckFiles) {
            try {
                fileExtension.put(fileType, FileType.fromExtension(FileUtils.getExtension(fileType)));
            }catch(Exception e) { // 如果出现异常则返回null或空
                waitConvertErrorAlertDialog(null, e.getMessage());
                return new LinkedHashMap<>();
            }
        }

        return fileExtension;
    }

    public void checkFileFormat(boolean needCheckExtraFiles, CheckFileCallBack checkFileCallBack) {
        FCLWaitDialog fclWaitDialog = enableWaitDialog("正在检测内部文件格式中，请稍等…");

        new Thread(() -> {
            try {
                Thread.sleep(888);

                LinkedHashMap<String, FileType> fileExtension = getFileExtension(needCheckExtraFiles);
                if(fileExtension.isEmpty()) {
                    fclWaitDialog.dismiss();
                    return;
                }

                if(checkAllFileExist(fileExtension)) checkFileCallBack.onSuccess(fileExtension);
                else checkFileCallBack.onFail(null);
                
                fclWaitDialog.dismiss();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * 检测APK内部的文件是否合法，如果不合法会弹出一个弹窗提醒
     *
     * @param fileExtension 文件信息，String是对应assets目录下文件名称，FileType为文件类型的枚举型
     * @return 只要有一个文件不合法则返回“false”
    **/
    protected boolean checkAllFileExist(LinkedHashMap<String, FileType> fileExtension) {
        Set<String> strings = fileExtension.keySet();

        for(String s : strings) {
            try(InputStream open = activity.getAssets().open(s)) {
                if(fileExtension.get(s) == FileType.IMAGE) {
                    Optional<Bitmap> bitmap = ImageUtil.load(open);
                    if(bitmap.isEmpty()) {
                        waitConvertErrorAlertDialog(null, "图片“" + s + "存在问题，请确保该图片大小和分辨率符合要求！");
                        return false;
                    }
                    bitmap.get().recycle();
                }else if(fileExtension.get(s) == FileType.JSON) {
                    try {
                        new JSONObject(IOUtils.readFullyAsString(open));
                    }catch(JSONException e) {
                        waitConvertErrorAlertDialog(null, "文件“" + s + "解析错误，请尝试重新制作你的APK直装包！");
                        return false;
                    }
                }
            }catch(IOException e) {
                waitConvertErrorAlertDialog(null, "文件“" + s + "不存在，请尝试重新制作你的APK直装包！");
                return false;
            }
        }

        return true;
    }

    protected void waitConvertErrorAlertDialog(FCLWaitDialog fclWaitDialog, String errorMessage) {
        if(fclWaitDialog == null) {
            enableAlertDialog(errorMessage);
            return;
        }

        if(!fclWaitDialog.isClosed()) fclWaitDialog.dismiss();

        enableAlertDialog(errorMessage);
    }

    protected void enableAlertDialog(String message) {
        activity.runOnUiThread(() -> new FCLAlertDialog.Builder(activity)
                .setAlertLevel(FCLAlertDialog.AlertLevel.ALERT)
                .setTitle("严重错误")
                .setMessage(message)
                .setNegativeButton("确定", () -> System.exit(0))
                .setCancelable(false)
                .create()
                .show());
    }

    protected FCLWaitDialog enableWaitDialog(String message) {
        FCLWaitDialog fclWaitDialog = new FCLWaitDialog.Builder(activity)
                .setMessage(message)
                .setCancelable(false)
                .create();
        fclWaitDialog.show();

        activity.runOnUiThread(fclWaitDialog::show);

        return fclWaitDialog;
    }

    /**
     * 文件对照位置；比如“APK”中，在“assets”目录下的某个文件对应在外部存储哪个路径中
    **/
    public static class FileInfo {
        private final String internalPath;
        private Path externalPath;

        public FileInfo(String internalPath, Path externalPath) {
            this.internalPath = internalPath;
            this.externalPath = externalPath;
        }

        public String getInternalPath() {
            return internalPath;
        }

        public Path getExternalPath() {
            return externalPath;
        }

        public void setExternalPath(Path externalPath) {
            this.externalPath = externalPath;
        }
    }

    /**
     * 枚举型，根据文件后缀名返回对应类型
    **/
    public enum FileType {
        IMAGE(Set.of("png", "jpg", "jpeg", "bmp", "gif", "webp")),
        JSON(Set.of("json")),
        TEXT(Set.of("", "txt","properties"));

        private final Set<String> extensions;

        FileType(Set<String> extensions) {
            this.extensions = extensions;
        }

        /**
         * 通过扩展名找到对应的枚举类型
         *
         * @param extension 传入一个文件扩展名
         * @return 返回一个包含该文件扩展名的枚举型
        **/
        public static FileType fromExtension(String extension) {
            if(extension == null) throw new IllegalArgumentException("未知文件格式，请将问题反馈给整合包作者！");

            // 遍历所有枚举值
            for(FileType fileType : FileType.values()) {
                if(fileType.extensions.contains(extension.toLowerCase())) return fileType;
            }
            // 如果没有找到匹配的枚举类型
            throw new IllegalArgumentException("未知文件格式“" + extension + "”，请确保APK的“assets/app_config”目录下对应文件格式正确且有效！");
        }
    }
    public interface CheckFileCallBack {
        <T> void onSuccess(T data);
        void onFail(Exception e);
    }
}