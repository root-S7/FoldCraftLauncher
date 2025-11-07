package com.root.manager

import android.content.res.AssetManager
import com.google.gson.Gson
import com.root.model.ThemeConfig
import com.tungsten.fcl.FCLApplication
import com.tungsten.fclauncher.utils.AssetsPath
import java.io.InputStreamReader

object ThemeManager {

    private var config: ThemeConfig? = null

    fun get(): ThemeConfig {
        if(config == null) loadConfig()
        return config!!
    }

    private fun loadConfig() {
        try {
            val context = FCLApplication.INSTANCE().applicationContext
            val am: AssetManager = context.assets
            am.open(AssetsPath.THEME).use { input ->
                InputStreamReader(input, Charsets.UTF_8).use { reader ->
                    config = Gson().fromJson(reader, ThemeConfig::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            config = ThemeConfig()
        }
    }
}