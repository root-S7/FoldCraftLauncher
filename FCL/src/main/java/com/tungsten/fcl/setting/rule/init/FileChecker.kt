package com.tungsten.fcl.setting.rule.init

import com.mio.CRendererManager.CRenderer_TYPE
import com.mio.CRendererManager.CUSTOM_RENDERER_GSON
import com.mio.data.Renderer
import com.tungsten.fcl.game.*
import com.tungsten.fcl.setting.*
import com.tungsten.fcl.setting.rule.core.*
import com.tungsten.fcl.util.FileType
import com.tungsten.fcl.util.GameRuleUtils.GAME_RULES_GSON
import com.tungsten.fcl.util.GameRuleUtils.RULES_TYPE
import com.tungsten.fclauncher.utils.AssetsPath
import com.tungsten.fclauncher.utils.FCLPath.*

import kotlinx.coroutines.*
import kotlin.io.path.pathString

object FileChecker {
    val checkFiles: Map<InitCheckFile, FileCheckRule?> = mapOf(
        InitCheckFile(AssetsPath.SETTINGS) to null,
        InitCheckFile(AssetsPath.AUTH_SERVER) to null,
        InitCheckFile(AssetsPath.GAME_VERSION) to null,
        InitCheckFile(AssetsPath.DK_IMG, DK_BACKGROUND_PATH) to null,
        InitCheckFile(AssetsPath.LT_IMG, LT_BACKGROUND_PATH) to null,
        InitCheckFile(AssetsPath.AUTH_LIB, AUTHLIB_INJECTOR_PATH) to null,
        InitCheckFile(AssetsPath.CURSOR, "${FILES_DIR}/cursor.png") to null,
        InitCheckFile(AssetsPath.CONFIG_VERSION, "${CONFIG_DIR}/version") to null,
        InitCheckFile(AssetsPath.MENU_ICON, "${FILES_DIR}/menu_icon.png") to null,
        InitCheckFile(AssetsPath.LAUNCHER_CONFIG) to configRule(), // 特殊：不需要释放到对应目录，而是通过InstallResources类的installConfig方法手动安装
        InitCheckFile(AssetsPath.RULES) to jsonRule<LinkedHashMap<String, Set<LaunchRule>>>(RULES_TYPE, GAME_RULES_GSON),
        InitCheckFile(AssetsPath.CUSTOM_RENDERER) to jsonRule<MutableSet<Renderer>>(CRenderer_TYPE, CUSTOM_RENDERER_GSON),
        InitCheckFile(AssetsPath.THEME) to themeRule(), // 特殊：不需要释放到对应目录，而是让它通过ThemeEngine的Flow更新就行
        InitCheckFile(AssetsPath.MENU, "${FILES_DIR}/menu_setting.json") to jsonRule(MenuSetting::class.java),
        InitCheckFile(AssetsPath.DEF_CONTROL, "${CONTROLLER_DIR}/00000000.json") to jsonRule(Controller::class.java)
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