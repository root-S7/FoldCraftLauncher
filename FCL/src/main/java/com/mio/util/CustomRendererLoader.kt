package com.mio.util

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import com.google.gson.JsonObject
import com.mio.data.Renderer
import com.mio.plugin.PluginManager
import com.tungsten.fcl.FCLApp.getAppContext
import com.tungsten.fclauncher.utils.FCLPath.NATIVE_LIB_DIR
import java.io.File

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

fun checkRendererSo(r: Renderer): Boolean {
    val boatEnv = r.boatEnv
    val pojavEnv = r.pojavEnv

    require(!boatEnv.isNullOrEmpty() || !pojavEnv.isNullOrEmpty()) {
        "BoatEnv和PojavEnv都为空"
    }

    val list = buildList {
        add(r.glName)
        add(r.eglName)
        boatEnv?.let { addAll(it) }
        pojavEnv?.let { addAll(it) }
    }

    list.asSequence()
        .flatMap { it.split(Regex("[^A-Za-z0-9_.-]+")).asSequence() }
        .filter { it.endsWith(".so") }
        .distinct()
        .forEach { so ->
            val file = File(NATIVE_LIB_DIR, so)
            Log.d("事件", "缺少native库：${file.absolutePath}")
            require(file.exists()) { "缺少native库：${file.absolutePath}" }
        }

    return true
}