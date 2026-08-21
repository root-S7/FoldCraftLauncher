package com.tungsten.fclauncher.utils

class AssetsPath {
    companion object Companion {
        private const val PATH = "app_config"
        private const val IMAGE = "$PATH/settings_launcher_pictures"

        const val DEF_CONTROL = "controllers/00000000.json"
        const val CONFIG_VERSION = "$PATH/version"
        const val RULES = "$PATH/launcher_rules.json"
        const val SETTINGS = "$PATH/general_setting.properties"
        const val AUTH_SERVER = "$PATH/authlib_injector_server.json"
        const val GAME_VERSION = ".minecraft/version"
        const val CUSTOM_RENDERER = "$PATH/custom_renderer.json"
        const val LAUNCHER_CONFIG = "$PATH/config.json"
        const val MENU = "$PATH/menu_setting.json"
        const val AUTH_LIB = "game/authlib-injector.jar"
        const val CURSOR = "$IMAGE/cursor.png"
        const val DK_IMG = "$IMAGE/dk.png"
        const val LT_IMG = "$IMAGE/lt.png"
        const val MENU_ICON = "$IMAGE/menu_icon.png"

        @JvmStatic
        fun addPrefix(path: String): String {
            return if(path.startsWith("/")) "assets$path"
            else "/assets/$path"
        }
    }
}