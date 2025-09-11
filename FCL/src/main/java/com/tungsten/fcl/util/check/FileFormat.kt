package com.tungsten.fcl.util.check

import android.os.Looper.getMainLooper
import android.os.Looper.myLooper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tungsten.fcl.setting.Config.fromJson
import com.tungsten.fcl.setting.ConfigHolder.*
import com.tungsten.fcl.setting.Controller
import com.tungsten.fcl.setting.MenuSetting
import com.tungsten.fcl.util.AndroidUtils.*
import com.tungsten.fcl.util.FileInfo
import com.tungsten.fcl.util.FileType.Companion.fromExtension
import com.tungsten.fcl.util.check.rule.FileCheckRule
import com.tungsten.fclauncher.utils.AssetsPath.Companion.AUTH_LIB
import com.tungsten.fclauncher.utils.AssetsPath.Companion.AUTH_SERVER
import com.tungsten.fclauncher.utils.AssetsPath.Companion.CONFIG_VERSION
import com.tungsten.fclauncher.utils.AssetsPath.Companion.CURSOR
import com.tungsten.fclauncher.utils.AssetsPath.Companion.CUSTOM_RENDERER
import com.tungsten.fclauncher.utils.AssetsPath.Companion.DEF_CONTROL
import com.tungsten.fclauncher.utils.AssetsPath.Companion.DK_IMG
import com.tungsten.fclauncher.utils.AssetsPath.Companion.GAME_VERSION
import com.tungsten.fclauncher.utils.AssetsPath.Companion.LAUNCHER_CONFIG
import com.tungsten.fclauncher.utils.AssetsPath.Companion.LT_IMG
import com.tungsten.fclauncher.utils.AssetsPath.Companion.MENU
import com.tungsten.fclauncher.utils.AssetsPath.Companion.MENU_ICON
import com.tungsten.fclauncher.utils.AssetsPath.Companion.RULES
import com.tungsten.fclauncher.utils.AssetsPath.Companion.SETTINGS
import com.tungsten.fclauncher.utils.FCLPath.*
import com.tungsten.fclcore.util.gson.fakefx.factories.JavaFxPropertyTypeAdapterFactory
import com.tungsten.fclcore.util.io.IOUtils.readFullyAsString
import kotlin.io.path.pathString


class FileFormat(vararg extraNeedFile: String) {
    val checkFiles: MutableSet<FileInfo> = mutableSetOf(
        FileInfo(RULES),
        FileInfo(SETTINGS),
        FileInfo(AUTH_SERVER),
        FileInfo(CUSTOM_RENDERER),
        FileInfo(AUTH_LIB, AUTHLIB_INJECTOR_PATH),
        FileInfo(CURSOR, "$FILES_DIR/cursor.png"),
        FileInfo(CONFIG_VERSION, "$CONFIG_DIR/version"),
        FileInfo(LAUNCHER_CONFIG, CONFIG_PATH.pathString, checkConfig()),
        FileInfo(MENU_ICON, "$FILES_DIR/menu_icon.png"), FileInfo(GAME_VERSION),
        FileInfo(DK_IMG, DK_BACKGROUND_PATH), FileInfo(LT_IMG, LT_BACKGROUND_PATH),
        FileInfo(MENU, "$FILES_DIR/menu_setting.json", typeJsonCheck(clazz = MenuSetting::class.java)),
        FileInfo(DEF_CONTROL, "$CONTROLLER_DIR/00000000.json", typeJsonCheck(GsonBuilder()
            .registerTypeAdapterFactory(JavaFxPropertyTypeAdapterFactory(true, true))
            .setPrettyPrinting()
            .create(), Controller::class.java)
        ),
    )

    init {
        extraNeedFile.filter {
            it.isNotBlank()
        }.mapTo(checkFiles) {
            FileInfo(it.trim(), null, null)
        }
    }

    /**
     * 检测所有文件，如果执行到某个文件检测结果为false则抛出文件不合法异常
     * 必须在非主线程上执行，因为某些文件检测设计到网络请求
    **/
    fun checkFiles(): Boolean = checkFiles.filter {
        it.assPath.isNotBlank()
    }.also {
        if(myLooper() == getMainLooper()) throw IllegalStateException("checkFiles()方法不可以在主线程内执行！")
    }.all { it ->
        val path = it.assPath.trim()
        val fileType = fromExtension(path.substringAfterLast('.', ""))
        it.getCheckRule(fileType).check(path).also { ok ->
            if(!ok) throw IllegalStateException("文件『$path』未通过校验，请重新制作直装包！")
        }
    }

    private fun typeJsonCheck(gson: Gson = GsonBuilder().setPrettyPrinting().create(), clazz: Class<*>): FileCheckRule = FileCheckRule {
        openAssets(null, it).use { input ->
            input.bufferedReader().use { reader ->
                gson.fromJson(reader, clazz) != null
            }
        }
    }

    private fun checkConfig(): FileCheckRule = FileCheckRule {
        runCatching {
            val configString = readFullyAsString(openAssets(null, it))
            val config = fromJson(configString)

            config != null && validateSelectedPath(config)
        }.getOrElse { e ->
            throw e
        }
    }
}