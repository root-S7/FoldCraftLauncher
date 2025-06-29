package com.tungsten.fcl.util;

import android.app.Activity;
import android.graphics.Bitmap;

import com.google.gson.JsonObject;
import com.mio.util.ImageUtil;
import com.tungsten.fcl.setting.Controller;
import com.tungsten.fcl.setting.MenuSetting;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog;
import com.tungsten.fcllibrary.component.dialog.FCLWaitDialog;

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
    protected final Set<FileInfo<?>> defaultCheckFiles;
    protected Set<String> extraNeedCheckInternalFile = new HashSet<>();
    protected final Activity activity;

    /**
     * 创建一个对象
     * @param activity 当前活动页
     * @param extraNeedFile 需要额外检查的文件
    **/
    public CheckFileFormat(Activity activity, String... extraNeedFile) {
        if(extraNeedFile != null && (extraNeedFile.length > 0)) {
            Arrays.stream(extraNeedFile)
                    .filter(file -> file != null && !file.isEmpty())  // 排除 null 和 空字符串
                    .forEach(extraNeedCheckInternalFile::add);  // 添加到集合中
        }

        FCLPath.loadPaths(activity);

        this.activity = activity;

        // 以下文件是必须要检查的
        defaultCheckFiles = Set.of(
                new FileInfo<>(FCLPath.ASSETS_CONFIG_JSON, null, JsonObject.class),
                new FileInfo<>(FCLPath.ASSETS_MENU_SETTING_JSON, Paths.get(FCLPath.FILES_DIR + "/menu_setting.json"), MenuSetting.class),
                new FileInfo<>(FCLPath.ASSETS_AUTH_INJECTOR_SERVER_JSON, null, null),
                new FileInfo<>(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/lt.png", Paths.get(FCLPath.LT_BACKGROUND_PATH), null),
                new FileInfo<>(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/dk.png", Paths.get(FCLPath.DK_BACKGROUND_PATH), null),
                new FileInfo<>(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/cursor.png", Paths.get(FCLPath.FILES_DIR + "/cursor.png"), null),
                new FileInfo<>(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/menu_icon.png", Paths.get(FCLPath.FILES_DIR + "/menu_icon.png"), null),
                new FileInfo<>(FCLPath.ASSETS_DEFAULT_CONTROLLER, null, Controller.class)
        );
    }

    public Set<FileInfo<?>> getDefaultCheckFiles() {
        return defaultCheckFiles;
    }

    public Set<String> getExtraNeedCheckInternalFile() {
        return extraNeedCheckInternalFile;
    }

    public void setExtraNeedCheckInternalFile(Set<String> extraNeedCheckInternalFile) {
        this.extraNeedCheckInternalFile = extraNeedCheckInternalFile;
    }

    /**
     * 将“defaultCheckFiles”数据结构转换成“Set<String>”
     *
     * @param needCheckExtraFiles 额外增加的文件是否也加入到“Set<String>”
     * @return 返回一个“Set<String>”数据结构
    **/
    protected Set<String> transformDataStructure(boolean needCheckExtraFiles) {
        Set<String> collect = defaultCheckFiles.stream().filter(
                fileInfo -> fileInfo != null && fileInfo.getInternalPath() != null && !fileInfo.getInternalPath().isEmpty()
        ).map(FileInfo::getInternalPath).collect(Collectors.toSet());

        if(needCheckExtraFiles) collect.addAll(extraNeedCheckInternalFile);

        return collect;
    }

    /**
     * 获取所有“需要检测的文件”后缀名是什么
     *
     * @param needCheckExtraFiles 是否检测额外增加的文件
     * @return 返回一个map表，使用枚举型保存着所有文件类型；只要有一个文件后缀名不在FileType的范围内则反回空表
    **/
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

    /**
     * getFileExtension + checkAllFileLegal合体版
     *
     * @param needCheckExtraFiles 是否检测额外增加的文件
     * @param checkFileCallBack 文件合法性的回调
    **/
    public void checkFileFormat(boolean needCheckExtraFiles, CheckFileCallBack checkFileCallBack) {
        FCLWaitDialog fclWaitDialog = enableWaitDialog("正在检测内部文件格式中，请稍等…");

        new Thread(() -> {
            LinkedHashMap<String, FileType> fileExtension = getFileExtension(needCheckExtraFiles);

            activity.runOnUiThread(() -> {
                if(fileExtension == null || fileExtension.isEmpty()) {
                    fclWaitDialog.dismiss();
                    if(checkFileCallBack != null) checkFileCallBack.onFail(null);
                }
            });

            boolean result = checkAllFileLegal(fileExtension);
            activity.runOnUiThread(() -> {
                if(result && checkFileCallBack != null) checkFileCallBack.onSuccess(fileExtension);
                else if(checkFileCallBack != null) checkFileCallBack.onFail(null);

                fclWaitDialog.dismiss();
            });
        }).start();
    }

    /**
     * 检测APK内部的文件是否合法，如果不合法会弹出一个弹窗提醒
     *
     * @param fileExtension 文件信息，String是对应assets目录下文件名称，FileType为文件类型的枚举型
     * @return 只要有一个文件不合法则返回“false”
    **/
    protected boolean checkAllFileLegal(final LinkedHashMap<String, FileType> fileExtension) {
        Set<String> strings = fileExtension.keySet();

        for(String s : strings) {
            try(InputStream open = activity.getAssets().open(s)) {
                if(fileExtension.get(s) == FileType.IMAGE) {
                    Optional<Bitmap> bitmap = ImageUtil.load(open);
                    if(bitmap.isEmpty()) throw new FileParseException("图片“" + s + "存在问题，请确保该图片大小和分辨率符合要求！");

                    bitmap.get().recycle();
                }else if(fileExtension.get(s) == FileType.JSON) {
                    Optional<FileInfo<?>> matchedFile = defaultCheckFiles.stream().filter(
                            fileInfo -> fileInfo.getInternalPath().equals(s)
                    ).findFirst();

                    if(matchedFile.isPresent()) {
                        Object o = AndroidUtils.tryDeserialize(open, matchedFile.get().configFileType, false);
                        if(o == null) throw new FileParseException("文件“" + s + "”解析错误，请尝试重新制作你的APK直装包！");

                    }else throw new FileParseException("不合法的文件“" + s + "”，我认为你反编译了APK并修改了该模块逻辑导致程序执行错误！");
                }
            }catch(IOException e) {
                activity.runOnUiThread(() -> waitConvertErrorAlertDialog(null, "文件“" + s + "”不存在，请尝试重新制作你的APK直装包！"));
                return false;
            }catch(Exception e) {
                activity.runOnUiThread(() -> waitConvertErrorAlertDialog(null, e.getMessage()));
                return false;
            }
        }

        return true;
    }

    /**
     * 如果“FCLWaitDialog”存在则尝试关闭它并显示错误弹窗
     *
     * @param fclWaitDialog 等待弹窗对象
     * @param errorMessage 需要展示的错误内容
    **/
    protected void waitConvertErrorAlertDialog(FCLWaitDialog fclWaitDialog, String errorMessage) {
        if(fclWaitDialog == null) {
            enableAlertDialog(errorMessage);
            return;
        }

        fclWaitDialog.dismiss();
        enableAlertDialog(errorMessage);
    }

    /**
     * 新建一个错误弹窗，该弹窗点击“确认”后直接退出应用
     *
     * @param message 弹窗显示内容
    **/
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

    /**
     * 新建一个等待弹窗，提醒用户目前存在耗时操作
     *
     * @param message 弹窗显示内容
     * @return 返回一个等待弹窗上下文
    **/
    protected FCLWaitDialog enableWaitDialog(String message) {
        return new FCLWaitDialog.Builder(activity)
                .setMessage(message)
                .setCancelable(false)
                .create()
                .showDialog();
    }

    /**
     * 需要检测的文件信息；比如“APK”中，在“assets”目录下的某个文件对应在外部存储哪个路径中和生成的对象
    **/
    public static class FileInfo<T> {
        private final String internalPath;
        private Path externalPath;
        private final Class<T> configFileType;
        private Object fileObject;

        public FileInfo(String internalPath, Path externalPath, Class<T> configFileType) {
            this.internalPath = internalPath;
            this.externalPath = externalPath;
            this.configFileType = configFileType;
        }

        public FileInfo<T> setFileObject(Object fileObject) {
            this.fileObject = fileObject;
            return this;
        }

        public Object getFileObject() {
            return fileObject;
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

        public Class<T> getConfigFileType() {
            return configFileType;
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

    /**
     * 文件检测后的回调
    **/
    public interface CheckFileCallBack {
        <T> void onSuccess(T data);
        void onFail(Exception e);
    }
}