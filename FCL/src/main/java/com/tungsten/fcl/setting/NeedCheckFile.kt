package com.tungsten.fcl.setting

import com.mio.manager.GameRulesManager

/**
 * 需要检测的文件信息内容
 * @param assPath APK中，“assets”目录下对应文件位置，不包含“assets”开头
 * @param outPath 文件将写入手机路径，可为 null
**/
data class NeedCheckFile(val assPath: String, val outPath: String? = null) {
    override fun equals(other: Any?): Boolean = other is NeedCheckFile && assPath == other.assPath

    override fun hashCode(): Int = assPath.hashCode()

    override fun toString(): String = GameRulesManager.GSON.toJson(this)
}