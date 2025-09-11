package com.tungsten.fcl.util

import com.tungsten.fcl.setting.rules.GameRulesManager.Companion.GSON
import com.tungsten.fcl.util.FileType.Companion.getDefaultRule
import com.tungsten.fcl.util.check.rule.FileCheckRule

/**
 * 文件信息
 * @param assPath APK中，“assets”目录下对应文件位置，不包含“assets”开头
 * @param outPath 文件将写入手机路径，可为 null
 * @param rule 文件检查规则，如果为 null，则使用 FileType 默认规则
 **/
class FileInfo(val assPath: String, val outPath: String? = null, private val rule: FileCheckRule? = null) {

    fun getCheckRule(fileType: FileType): FileCheckRule {
        return rule ?: getDefaultRule(fileType)
    }

    override fun equals(other: Any?): Boolean {
        return other is FileInfo && assPath == other.assPath
    }

    override fun hashCode(): Int {
        return assPath.hashCode()
    }

    override fun toString(): String {
        return GSON.toJson(this)
    }
}