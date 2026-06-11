package com.mio.manager

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mio.data.Renderer
import com.tungsten.fcl.setting.rule.core.VersionRule
import com.tungsten.fcl.util.AndroidUtils
import com.tungsten.fcl.util.gson.RuleJavaSetAdapter
import com.tungsten.fcl.util.gson.RuleRendererSetAdapter
import com.tungsten.fclauncher.utils.AssetsPath
import com.tungsten.fclcore.game.JavaVersion
import com.tungsten.fclcore.util.gson.URLTypeAdapter
import com.tungsten.fclcore.util.io.IOUtils
import java.net.URL
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.stream.Collectors

class GameRulesManager : LinkedHashMap<String, VersionRule>() {

    companion object {
        @JvmStatic
        val GSON: Gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(URL::class.java, URLTypeAdapter())
            .registerTypeAdapter(object : TypeToken<LinkedHashSet<Renderer>>() {}.type,
                RuleRendererSetAdapter()
            )
            .registerTypeAdapter(object : TypeToken<LinkedHashSet<JavaVersion>>() {}.type,
                RuleJavaSetAdapter()
            )
            .create()

        @JvmStatic
        fun fromJson(jsonString: String): GameRulesManager {
            return try {
                GSON.fromJson(jsonString, GameRulesManager::class.java)
            }catch(_: Exception) {
                GameRulesManager()
            }
        }

        @JvmStatic
        fun fromJson(context: Context): GameRulesManager {
            return try {
                context.assets.open(AssetsPath.Companion.RULES).use { inputStream ->
                    fromJson(IOUtils.readFullyAsString(inputStream))
                }
            }catch(_: Exception) {
                GameRulesManager()
            }
        }
    }

    fun getLauncherRules(): Map<String, VersionRule> = this

    fun getVersionRule(version: String): VersionRule {
        return getOrDefault(version, entries.stream()
            .filter { AndroidUtils.isRegexMatch(version, it.key) }
            .map { it.value }
            .findFirst()
            .orElse(VersionRule())
        )
    }

    fun getVersionRules(version: String): LinkedHashSet<VersionRule> {
        return entries.stream()
            .filter { version == it.key || AndroidUtils.isRegexMatch(version, it.key) }
            .map { it.value }
            .collect(Collectors.toCollection { LinkedHashSet() })
    }

    override fun toString(): String = GSON.toJson(this)
}