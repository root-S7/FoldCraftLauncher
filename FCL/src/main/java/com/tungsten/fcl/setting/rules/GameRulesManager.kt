package com.tungsten.fcl.setting.rules

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mio.data.Renderer
import com.tungsten.fcl.setting.rules.extend.VersionRule
import com.tungsten.fcl.util.AndroidUtils.isRegexMatch
import com.tungsten.fcl.util.gson.RuleJavaSetAdapter
import com.tungsten.fcl.util.gson.RuleRendererSetAdapter
import com.tungsten.fclauncher.utils.AssetsPath.Companion.RULES
import com.tungsten.fclcore.game.JavaVersion
import com.tungsten.fclcore.util.gson.URLTypeAdapter
import com.tungsten.fclcore.util.io.IOUtils
import java.net.URL
import java.util.*
import java.util.stream.Collectors

class GameRulesManager : LinkedHashMap<String, VersionRule>() {

    companion object {
        @JvmStatic
        val GSON: Gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(URL::class.java, URLTypeAdapter())
            .registerTypeAdapter(object : TypeToken<LinkedHashSet<Renderer>>() {}.type, RuleRendererSetAdapter())
            .registerTypeAdapter(object : TypeToken<LinkedHashSet<JavaVersion>>() {}.type, RuleJavaSetAdapter())
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
                context.assets.open(RULES).use { inputStream ->
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
            .filter { isRegexMatch(version, it.key) }
            .map { it.value }
            .findFirst()
            .orElse(VersionRule())
        )
    }

    fun getVersionRules(version: String): LinkedHashSet<VersionRule> {
        return entries.stream()
            .filter { version == it.key || isRegexMatch(version, it.key) }
            .map { it.value }
            .collect(Collectors.toCollection { LinkedHashSet() })
    }

    override fun toString(): String = GSON.toJson(this)
}