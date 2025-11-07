package com.tungsten.fcl.util

import com.tungsten.fcl.util.check.DefaultCheck
import com.tungsten.fcl.util.check.rule.BaseRule
import com.tungsten.fcl.util.check.rule.FileCheckRule
import com.tungsten.fcl.util.check.rule.ImageRule
import com.tungsten.fcl.util.check.rule.JsonRule
import com.tungsten.fcl.util.check.rule.SharedPreferencesRule

enum class FileType(val extensions: Set<String>) {
    @DefaultCheck(BaseRule::class)
    TEXT(setOf("", "txt", "properties")),

    @DefaultCheck(SharedPreferencesRule::class)
    XML(setOf("xml")),

    @DefaultCheck(JsonRule::class)
    JSON(setOf("json")),

    @DefaultCheck(ImageRule::class)
    IMAGE(setOf("png", "jpg", "jpeg", "bmp", "gif", "webp")),

    @DefaultCheck(BaseRule::class)
    ZIP(setOf("zip", "rar", "7z", "jar", "xz", "tar", "wim", "gzip"));

    companion object {
        /**
         * 检查文件是什么格式
         * @param extension 若为null直接抛出异常，对于无法匹配的文件格式则自动识别为文本文件
         **/
        fun fromExtension(extension: String?): FileType {
            requireNotNull(extension) {
                "未知文件格式，请将问题反馈给整合包作者！"
            }
            return entries.firstOrNull {
                it.extensions.contains(extension.lowercase())
            } ?: TEXT
        }

        /**
         * 获取枚举类型的默认检测规则注解
         * 若未标记，则抛出异常
         * @param type 文件类型（FileType中枚举型）
         **/
        fun getDefaultRule(type: FileType?): FileCheckRule {
            requireNotNull(type) { "类型不能为空！" }

            val field = FileType::class.java.getField(type.name)
            val annotation = field.getAnnotation(DefaultCheck::class.java)
                ?: throw IllegalStateException("FileType枚举型中，『${type.name}』缺少『@DefaultCheck』注解")

            val clazz = annotation.ruleClass.java
            return try {
                val instance = clazz.getField("INSTANCE").get(null)
                instance as? FileCheckRule ?: clazz.getDeclaredConstructor().newInstance()
                ?: throw IllegalStateException("默认规则『${clazz.name}』的INSTANCE不是有效的FileCheckRule类型，也无法通过构造创建实例")
            }catch(_: NoSuchFieldException) {
                clazz.getDeclaredConstructor().newInstance()
                    ?: throw IllegalStateException("默认规则『${clazz.name}』必须实现FileCheckRule接口")
            }
        }
    }
}