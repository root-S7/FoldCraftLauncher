package com.tungsten.fcl.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.tungsten.fcl.setting.rule.core.LaunchRule
import com.tungsten.fclcore.util.gson.LaunchRulesAdapter
import com.tungsten.fclauncher.utils.AssetsPath
import com.tungsten.fclcore.util.StringUtils.isRegexMatch
import com.tungsten.fclcore.util.io.IOUtils
import java.lang.reflect.Type
import java.util.stream.Collectors

object GameRuleUtils {

    @JvmField
    val RULES_TYPE: Type = object : TypeToken<LinkedHashMap<String, MutableSet<LaunchRule>>>() {}.type

    @JvmField
    val GAME_RULES_GSON: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(RULES_TYPE, LaunchRulesAdapter())
        .create()

    @JvmStatic
    fun fromJson(jsonString: String): LinkedHashMap<String, MutableSet<LaunchRule>> {
        return try {
            GAME_RULES_GSON.fromJson(jsonString, RULES_TYPE)
        } catch (_: Exception) {
            linkedMapOf()
        }
    }

    @JvmStatic
    fun fromJson(context: Context): LinkedHashMap<String, MutableSet<LaunchRule>> {
        return try {
            context.assets.open(AssetsPath.RULES).use { inputStream ->
                fromJson(IOUtils.readFullyAsString(inputStream))
            }
        }catch(_: Exception) {
            linkedMapOf()
        }
    }

    @JvmStatic
    fun LinkedHashMap<String, MutableSet<LaunchRule>>.getVersionRules(version: String): MutableSet<LaunchRule> {
        return entries.stream()
            .filter { version == it.key || isRegexMatch(version, it.key) }
            .flatMap { it.value.stream() }
            .collect(Collectors.toCollection { HashSet() })
    }

    @JvmStatic
    fun <T : LaunchRule> MutableSet<LaunchRule>?.findRule(clazz: Class<T>?): T? = runCatching {
        this?.firstOrNull { it.javaClass == clazz }
            ?.let { clazz?.cast(it) }
    }.getOrNull()
}