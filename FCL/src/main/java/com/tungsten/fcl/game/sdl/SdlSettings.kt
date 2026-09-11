/*
 * Fold Craft Launcher
 * SDL 集成相关设置（SharedPreferences "launcher" 杂项设置）
 */
package com.tungsten.fcl.game.sdl

import android.content.Context
import com.tungsten.fcl.FCLApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SdlSettings {
    private val prefs get() = FCLApp.getAppContext().getSharedPreferences("launcher", Context.MODE_PRIVATE)

    private val _sdlAutoShowIme = MutableStateFlow(prefs.getBoolean("sdl_auto_show_ime", false))
    /** SDL 请求唤起输入法时，启动器侧是否响应（游戏内菜单开关） */
    @JvmStatic
    val sdlAutoShowIme = _sdlAutoShowIme.asStateFlow()

    @JvmStatic
    fun setSdlAutoShowIme(value: Boolean) {
        prefs.edit().putBoolean("sdl_auto_show_ime", value).apply()
        _sdlAutoShowIme.value = value
    }

    private val _gamepadInputMode = MutableStateFlow(
        GamepadInputMode.valueOf(prefs.getString("gamepad_input_mode", GamepadInputMode.MAPPED.name)!!)
    )
    /** 手柄输入模式：MAPPED=控制布局重映射，SDL_DIRECT=直通 SDL */
    @JvmStatic
    val gamepadInputMode = _gamepadInputMode.asStateFlow()

    @JvmStatic
    fun setGamepadInputMode(mode: GamepadInputMode) {
        prefs.edit().putString("gamepad_input_mode", mode.name).apply()
        _gamepadInputMode.value = mode
    }

    private val _gamepadInputModePrompted = MutableStateFlow(prefs.getBoolean("gamepad_input_mode_prompted", false))
    /** 手柄输入模式是否已通过首次弹窗确认过（确认后不再自动弹窗） */
    @JvmStatic
    val gamepadInputModePrompted = _gamepadInputModePrompted.asStateFlow()

    @JvmStatic
    fun isGamepadInputModePrompted(): Boolean = _gamepadInputModePrompted.value

    @JvmStatic
    fun setGamepadInputModePrompted(value: Boolean) {
        prefs.edit().putBoolean("gamepad_input_mode_prompted", value).apply()
        _gamepadInputModePrompted.value = value
    }
}
