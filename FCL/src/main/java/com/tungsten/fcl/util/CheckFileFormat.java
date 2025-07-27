package com.tungsten.fcl.util;

import static com.tungsten.fcl.util.AndroidUtils.tryDeserialize;
import static com.tungsten.fclauncher.utils.FCLPath.*;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.mio.util.ImageUtil;
import com.tungsten.fcl.setting.Controller;
import com.tungsten.fcl.setting.MenuSetting;
import com.tungsten.fclcore.util.io.FileUtils;

import java.io.FileNotFoundException;
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
    protected final Set<FileInfo<?>> defaultCheckFiles = Set.of(
            new FileInfo<>(ASSETS_CONFIG_JSON, null, JsonObject.class),
            new FileInfo<>(ASSETS_MENU_SETTING_JSON, Paths.get(FILES_DIR + "/menu_setting.json"), MenuSetting.class),
            new FileInfo<>(ASSETS_AUTH_INJECTOR_SERVER_JSON, null, null),
            new FileInfo<>(ASSETS_SETTING_LAUNCHER_PICTURES + "/lt.png", Paths.get(LT_BACKGROUND_PATH), null),
            new FileInfo<>(ASSETS_SETTING_LAUNCHER_PICTURES + "/dk.png", Paths.get(DK_BACKGROUND_PATH), null),
            new FileInfo<>(ASSETS_SETTING_LAUNCHER_PICTURES + "/cursor.png", Paths.get(FILES_DIR + "/cursor.png"), null),
            new FileInfo<>(ASSETS_SETTING_LAUNCHER_PICTURES + "/menu_icon.png", Paths.get(FILES_DIR + "/menu_icon.png"), null),
            new FileInfo<>(ASSETS_DEFAULT_CONTROLLER, null, Controller.class)
    );
    protected Set<String> extraNeedCheckInternalFile = new HashSet<>();

    /**
     * 创建一个对象
     * @param context 上下文
     * @param extraNeedFile 需要额外检查的文件
    **/
    public CheckFileFormat(@NonNull Context context, String... extraNeedFile) {
        if(extraNeedFile != null && (extraNeedFile.length > 0)) {
            Arrays.stream(extraNeedFile)
                    .filter(file -> file != null && !file.isEmpty())  // 排除 null 和 空字符串
                    .forEach(extraNeedCheckInternalFile::add);  // 添加到集合中
        }

        loadPaths(context.getApplicationContext());
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
    public LinkedHashMap<String, FileType> getFileExtension(boolean needCheckExtraFiles) throws Exception {
        LinkedHashMap<String, FileType> fileExtension = new LinkedHashMap<>();
        Set<String> internalNeedCheckFiles = transformDataStructure(needCheckExtraFiles);

        for(String fileType : internalNeedCheckFiles) {
            try {
                fileExtension.put(fileType, FileType.fromExtension(FileUtils.getExtension(fileType)));
            }catch(Exception e) { // 如果出现异常则返回null或空
                throw new Exception(e);
            }
        }

        return fileExtension;
    }

    /**
     * getFileExtension + checkAllFileLegal合体版
     *
     * @param needCheckExtraFiles 是否检测额外增加的文件
    **/
    public void checkFileFormat(boolean needCheckExtraFiles) throws Exception {
        LinkedHashMap<String, FileType> fileExtension = getFileExtension(needCheckExtraFiles);
        if(fileExtension == null) throw new Exception("待检测文件为空，无法完成本次任务！");

        boolean result = checkAllFileLegal(fileExtension);
        if(!result) throw new FileParseException("有一个文件存在错误，但不知晓具体错误和文件名称。请尝试重新制作APK！");
    }

    /**
     * 检测APK内部的文件是否合法，如果不合法或其它问题则抛出异常
     *
     * @param fileExtension 文件信息，String是对应assets目录下文件名称，FileType为文件类型的枚举型
     * @return 只要有一个文件不合法则返回“false”
    **/
    protected boolean checkAllFileLegal(final LinkedHashMap<String, FileType> fileExtension) throws Exception {
        Set<String> strings = fileExtension.keySet();

        for(String s : strings) {
            try(InputStream open = CONTEXT.getAssets().open(s)) {
                if(fileExtension.get(s) == FileType.IMAGE) {
                    Optional<Bitmap> bitmap = ImageUtil.load(open);
                    if(bitmap.isEmpty()) throw new FileParseException("图片“" + s + "存在问题，请确保该图片大小和分辨率符合要求！");

                    bitmap.get().recycle();
                }else if(fileExtension.get(s) == FileType.JSON) {
                    Optional<FileInfo<?>> matchedFile = defaultCheckFiles.stream().filter(
                            fileInfo -> fileInfo.getInternalPath().equals(s)
                    ).findFirst();

                    if(matchedFile.isPresent()) {
                        Object o = tryDeserialize(open, matchedFile.get().configFileType, false);
                        if(o == null) throw new FileParseException("文件“" + s + "”解析错误，请尝试重新制作你的APK直装包！");

                    }else throw new FileParseException("不合法的文件“" + s + "”，我认为你反编译了APK并修改了该模块逻辑导致程序执行错误！");
                }
            }catch(FileParseException e) {
                throw e;
            }catch(IOException e) {
                throw new FileNotFoundException("文件“" + s + "”不存在，请尝试重新制作你的APK直装包！");
            }catch(Exception e) {
                throw new Exception(e.getMessage());
            }
        }

        return true;
    }

    /**
     * 需要检测的文件信息；比如“APK”中，在“assets”目录下的某个文件对应在外部存储哪个路径中和生成的对象
    **/
    public static class FileInfo<T> {
        private final String internalPath;
        private Path externalPath;
        private final Class<T> configFileType;

        public FileInfo(String internalPath, Path externalPath, Class<T> configFileType) {
            this.internalPath = internalPath;
            this.externalPath = externalPath;
            this.configFileType = configFileType;
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
}