package com.tungsten.fcl.util;

import static com.tungsten.fcl.util.AndroidUtils.tryDeserialize;
import static com.tungsten.fcl.util.FileType.*;
import static com.tungsten.fclauncher.utils.FCLPath.*;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.*;

import com.google.gson.JsonObject;
import com.mio.util.ImageUtil;
import com.tungsten.fcl.setting.Controller;
import com.tungsten.fcl.setting.MenuSetting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckFileFormat {

    /**
     * 检查文件格式是否正确
    **/
    protected final Set<FileInfo<?>> defaultFiles = Set.of(
            new FileInfo<>(ASSETS_CONFIG_JSON, null, JsonObject.class),
            new FileInfo<>(ASSETS_MENU_SETTING_JSON, Paths.get(FILES_DIR + "/menu_setting.json"), MenuSetting.class),
            new FileInfo<>(ASSETS_AUTH_INJECTOR_SERVER_JSON, null, null),
            new FileInfo<>(ASSETS_SETTING_LAUNCHER_PICTURES + "/lt.png", Paths.get(LT_BACKGROUND_PATH), null),
            new FileInfo<>(ASSETS_SETTING_LAUNCHER_PICTURES + "/dk.png", Paths.get(DK_BACKGROUND_PATH), null),
            new FileInfo<>(ASSETS_SETTING_LAUNCHER_PICTURES + "/cursor.png", Paths.get(FILES_DIR + "/cursor.png"), null),
            new FileInfo<>(ASSETS_SETTING_LAUNCHER_PICTURES + "/menu_icon.png", Paths.get(FILES_DIR + "/menu_icon.png"), null),
            new FileInfo<>(ASSETS_DEFAULT_CONTROLLER, null, Controller.class),
            new FileInfo<>(ASSETS_LAUNCHER_RULES, null, JsonObject.class),
            new FileInfo<>(ASSETS_GENERAL_SETTING_PROPERTIES, null, null),
            new FileInfo<>(ASSETS_AUTHLIB_INJECTOR_JAR, null, null)
    );
    protected Set<String> extraInternalFile = new HashSet<>();

    /**
     * 创建一个对象
     * @param context 上下文
     * @param extraNeedFile 需要额外检查的文件
    **/
    public CheckFileFormat(@NonNull Context context, String... extraNeedFile) {
        if(extraNeedFile != null && (extraNeedFile.length > 0)) {
            Arrays.stream(extraNeedFile)
                    .filter(file -> file != null && !file.isEmpty())
                    .forEach(extraInternalFile::add);
        }

        loadPaths(context.getApplicationContext());
    }

    public Set<FileInfo<?>> getDefaultFiles() {
        return defaultFiles;
    }

    public Set<String> getExtraInternalFile() {
        return extraInternalFile;
    }

    public void setExtraInternalFile(Set<String> extraNeedCheckInternalFile) {
        this.extraInternalFile = extraNeedCheckInternalFile;
    }

    /**
     * 检测文件是否都合法
     *
     * @param checkExtraFiles 是否检测额外增加的文件
     * @throws Exception 只要有一个文件存在格式问题就抛出异常
     **/
    public void checkFileFormat(boolean checkExtraFiles) throws Exception {
        LinkedHashMap<String, FileType> needCheck = defaultFiles.stream()
                .map(FileInfo::getInternalPath)
                .collect(Collectors.toCollection(() -> {
                    Set<String> result = new LinkedHashSet<>();
                    if(checkExtraFiles && extraInternalFile != null) result.addAll(extraInternalFile);
                    return result;
                }))
                .stream()
                .filter(path -> path != null && !path.isEmpty())
                .collect(Collectors.toMap(path -> path, path -> {
                        String extension = "";
                        int lastDotIndex = path.lastIndexOf('.');
                        if(lastDotIndex > 0 && lastDotIndex < path.length() - 1) {
                            extension = path.substring(lastDotIndex + 1);
                        }
                        return FileType.fromExtension(extension);
                    }, (existing, replacement) -> existing, LinkedHashMap::new)
                );

        if(needCheck.isEmpty()) throw new Exception("待检测文件为空，无法完成本次任务！");

        boolean result = checkAllFileLegal(needCheck);
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
                if(fileExtension.get(s) == IMAGE) {
                    Optional<Bitmap> bitmap = ImageUtil.load(open);
                    if(bitmap.isEmpty()) throw new FileParseException("图片“" + s + "存在问题，请确保该图片大小和分辨率符合要求！");

                    bitmap.get().recycle();
                }else if(fileExtension.get(s) == JSON) {
                    Optional<FileInfo<?>> matchedFile = defaultFiles
                            .stream()
                            .filter(fileInfo -> fileInfo.getInternalPath().equals(s))
                            .findFirst();

                    if(matchedFile.isPresent()) {
                        Object o = tryDeserialize(open, matchedFile.get().configFileType, false);
                        if(o == null) throw new FileParseException("文件“" + s + "”解析错误，请尝试重新制作你的APK直装包！");
                    }else throw new FileParseException("不合法的文件“" + s + "”，我认为你反编译了APK并修改了该模块逻辑导致程序执行错误！");
                }
            }catch(IOException e) {
                throw new FileNotFoundException("文件“" + s + "”不存在，请尝试重新制作你的APK直装包！");
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

        @Override
        public boolean equals(@Nullable Object obj) {
            if(obj instanceof FileInfo) return internalPath.equals(((FileInfo<?>) obj).internalPath);
            else return false;
        }

        @Override
        public int hashCode() {
            return internalPath.hashCode();
        }
    }
}