package com.mio.util

import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.google.gson.JsonObject
import com.mio.plugin.PluginManager
import com.tungsten.fcl.FCLApp.getAppContext

/**
 * 动态将 JsonObject 中的所有字段转换为 Bundle
 */
fun buildBundle(config: JsonObject) = Bundle().apply {
    putBoolean(PluginManager.META_PLUGIN, true)

    for((key, element) in config.entrySet()) {
        val primitive = element?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asJsonPrimitive ?: continue

        when {
            primitive.isString -> putString(key, primitive.asString)
            primitive.isBoolean -> putBoolean(key, primitive.asBoolean)
            primitive.isNumber -> if(primitive.asString.contains('.')) { putDouble(key, primitive.asDouble) }else putInt(key, primitive.asInt)
        }
    }
}

/**
 * 构建假的 ApplicationInfo 并挂载 metaData 与 nativeLibraryDir
 */
fun buildApplicationInfo(packageName: String, metaData: Bundle) = ApplicationInfo().apply {
    this.packageName = packageName
    this.nativeLibraryDir = getAppContext().applicationInfo.nativeLibraryDir
    this.metaData = metaData
}