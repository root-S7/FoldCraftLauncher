package com.tungsten.fcl.game.check

import com.tungsten.fcl.setting.ConfigHolder
import com.tungsten.fcl.setting.Controller
import com.tungsten.fcl.setting.MenuSetting
import com.tungsten.fcl.setting.NeedCheckFile
import com.tungsten.fcl.util.FileType
import com.tungsten.fcl.game.check.rule.FileCheckRule
import com.tungsten.fcl.game.check.rule.configRule
import com.tungsten.fcl.game.check.rule.typeJsonRule
import com.tungsten.fclauncher.utils.AssetsPath
import com.tungsten.fclauncher.utils.FCLPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.path.pathString

object FileChecker {
    val checkFiles: Map<NeedCheckFile, FileCheckRule?> = mapOf(
        NeedCheckFile(AssetsPath.RULES) to null,
        NeedCheckFile(AssetsPath.SETTINGS) to null,
        NeedCheckFile(AssetsPath.AUTH_SERVER) to null,
        NeedCheckFile(AssetsPath.CUSTOM_RENDERER) to null,
        NeedCheckFile(AssetsPath.AUTH_LIB, FCLPath.AUTHLIB_INJECTOR_PATH) to null,
        NeedCheckFile(AssetsPath.CURSOR, "${FCLPath.FILES_DIR}/cursor.png") to null,
        NeedCheckFile(AssetsPath.CONFIG_VERSION, "${FCLPath.CONFIG_DIR}/version") to null,
        NeedCheckFile(AssetsPath.LAUNCHER_CONFIG, ConfigHolder.CONFIG_PATH.pathString) to configRule(),
        NeedCheckFile(AssetsPath.MENU_ICON, "${FCLPath.FILES_DIR}/menu_icon.png") to null,
        NeedCheckFile(AssetsPath.GAME_VERSION) to null,
        NeedCheckFile(AssetsPath.DK_IMG, FCLPath.DK_BACKGROUND_PATH) to null,
        NeedCheckFile(AssetsPath.LT_IMG, FCLPath.LT_BACKGROUND_PATH) to null,
        NeedCheckFile(AssetsPath.MENU, "${FCLPath.FILES_DIR}/menu_setting.json") to typeJsonRule(MenuSetting::class.java),
        NeedCheckFile(AssetsPath.DEF_CONTROL, "${FCLPath.CONTROLLER_DIR}/00000000.json") to typeJsonRule(Controller::class.java)
    )

    suspend fun checkFiles(): Boolean = withContext(Dispatchers.IO) {
        checkFiles
            .filter { it.key.assPath.isNotBlank() }
            .all {
                val path = it.key.assPath.trim()
                val fileType = FileType.fromExtension(path.substringAfterLast('.', ""))
                (it.value ?: fileType.defaultRule)(path)
            }
    }
}