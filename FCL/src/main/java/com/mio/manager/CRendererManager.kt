package com.mio.manager

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mio.data.Renderer
import com.tungsten.fcl.FCLApp.getAppContext
import com.tungsten.fclauncher.utils.AssetsPath
import com.tungsten.fclcore.util.gson.CustomRendererSetAdapter
import com.tungsten.fclcore.util.io.IOUtils.openAssets
import com.tungsten.fclcore.util.io.IOUtils.readFullyAsString
import java.lang.reflect.Type

object CRendererManager {
    @JvmField
    val CRenderer_TYPE: Type =object : TypeToken<MutableSet<Renderer>>() {}.type

    private var isInit = false

    val CUSTOM_RENDERER_GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(CRenderer_TYPE, CustomRendererSetAdapter(getAppContext().applicationInfo.nativeLibraryDir))
        .create()

    @JvmStatic
    val cRenderer: MutableSet<Renderer> = mutableSetOf()
        get() {
            if(!isInit) init()
            return field
        }

    @JvmStatic
    fun init() {
        isInit = true
        loadJson()
    }

    @JvmStatic
    private fun loadJson() {
        runCatching {
            val fileData = readFullyAsString(openAssets(null, AssetsPath.CUSTOM_RENDERER))
            val rendererSet: MutableSet<Renderer> = CUSTOM_RENDERER_GSON.fromJson(fileData, CRenderer_TYPE)
            cRenderer.addAll(rendererSet)
        }.onFailure {
            it.printStackTrace()
        }
    }
}