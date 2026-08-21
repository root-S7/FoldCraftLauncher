package com.tungsten.fcl.util

import com.tungsten.fcl.game.check.rule.*

enum class FileType(val extensions: Set<String>, val defaultRule: FileCheckRule) {
    TEXT(setOf("", "txt", "properties"), BaseRule),
    XML(setOf("xml"), SharedPreferencesRule),
    JSON(setOf("json"), JsonRule),
    IMAGE(setOf("png", "jpg", "jpeg", "bmp", "gif", "webp"), ImageRule),
    ZIP(setOf("zip", "rar", "7z", "jar", "xz", "tar", "wim", "gzip"), BaseRule);

    companion object {
        fun fromExtension(extension: String?): FileType = requireNotNull(extension) { "未知文件格式，请将问题反馈给整合包作者！" }
            .lowercase()
            .let { ext -> entries.find { ext in it.extensions } } ?: TEXT
    }
}