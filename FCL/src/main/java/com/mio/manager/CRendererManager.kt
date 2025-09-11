package com.mio.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mio.data.Renderer
import com.tungsten.fcl.util.AndroidUtils.*
import com.tungsten.fcl.util.gson.CustomRendererSetAdapter
import com.tungsten.fclauncher.utils.AssetsPath.Companion.CUSTOM_RENDERER
import com.tungsten.fclauncher.utils.FCLPath
import com.tungsten.fclcore.util.io.IOUtils

object CRendererManager {
    private var isInit = false

    @JvmStatic
    val cRenderer: MutableSet<Renderer> = mutableSetOf()
        get() {
            if (!isInit) init(FCLPath.CONTEXT)
            return field
        }

    @JvmStatic
    fun init(context: Context) {
        isInit = true
        loadJson(context)
    }

    @JvmStatic
    private fun loadJson(context: Context) {
        runCatching {
            val gson: Gson = GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(
                    object : TypeToken<MutableSet<Renderer>>() {}.type,
                    CustomRendererSetAdapter(context.applicationInfo.nativeLibraryDir)
                )
                .create()

            val fileData = IOUtils.readFullyAsString(openAssets(null, CUSTOM_RENDERER))
            val rendererSet: MutableSet<Renderer> = gson.fromJson(fileData, object : TypeToken<MutableSet<Renderer>>() {}.type)
            cRenderer.addAll(rendererSet)
        }.onFailure {
            it.printStackTrace()
        }
    }
}
