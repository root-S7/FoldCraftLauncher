package com.tungsten.fcl.util;

import java.util.Set;

public enum FileType {
    IMAGE(Set.of("png", "jpg", "jpeg", "bmp", "gif", "webp")),
    JSON(Set.of("json")),
    TEXT(Set.of("", "txt","properties")),
    ZIP(Set.of("zip", "rar", "7z", "jar", "xz", "tar", "wim", "gzip"));

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
        for(FileType fileType : FileType.values()) {
            if(fileType.extensions.contains(extension.toLowerCase())) return fileType;
        }
        throw new IllegalArgumentException("未知文件格式“" + extension + "”，请确保APK的“assets/app_config”目录下对应文件格式正确且有效！");
    }
}
