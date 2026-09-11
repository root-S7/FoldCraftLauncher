/*
 * Fold Craft Launcher
 * 首次手柄输入时的输入模式选择弹窗（对齐 ZalithLauncher2 的 GamepadModePromptDialog）
 */
package com.tungsten.fcl.game.sdl

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tungsten.fcl.R
import com.tungsten.fcl.databinding.DialogGamepadModePromptBinding
import com.tungsten.fcllibrary.component.dialog.FCLDialog
import com.tungsten.fcllibrary.util.ConvertUtils
import java.lang.ref.WeakReference

/**
 * 首次手柄输入时弹窗选择输入模式（确认前所有手柄输入都会被吞掉）。
 */
class GamepadModePromptDialog private constructor(activity: Activity) : FCLDialog(activity), View.OnClickListener {

    private var mode: GamepadInputMode
    private val binding = DialogGamepadModePromptBinding.inflate(LayoutInflater.from(activity))

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window?.setLayout(ConvertUtils.dip2px(activity, 380f), ViewGroup.LayoutParams.WRAP_CONTENT)
        setContentView(binding.root)

        binding.rowMapped.setOnClickListener(this)
        binding.rowSdl.setOnClickListener(this)
        binding.radioMapped.setOnClickListener(this)
        binding.radioSdl.setOnClickListener(this)
        binding.positive.setOnClickListener(this)

        // 默认选中当前设置的模式
        mode = SdlSettings.gamepadInputMode.value
        binding.radioMapped.isChecked = mode == GamepadInputMode.MAPPED
        binding.radioSdl.isChecked = mode == GamepadInputMode.SDL_DIRECT
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.row_mapped, R.id.radio_mapped -> {
                mode = GamepadInputMode.MAPPED
                binding.radioMapped.isChecked = true
                binding.radioSdl.isChecked = false
            }

            R.id.row_sdl, R.id.radio_sdl -> {
                mode = GamepadInputMode.SDL_DIRECT
                binding.radioMapped.isChecked = false
                binding.radioSdl.isChecked = true
            }

            R.id.positive -> {
                SdlSettings.setGamepadInputMode(mode)
                SdlSettings.setGamepadInputModePrompted(true)
                dismiss()
            }
        }
    }

    companion object {
        private var sInstance: WeakReference<GamepadModePromptDialog>? = null

        /**
         * 首次手柄输入时弹窗选择输入模式。
         * @return true 表示弹窗已显示或显示中，调用方应吞掉本次手柄输入；
         *         false 表示无需弹窗，按原逻辑处理
         */
        @JvmStatic
        fun checkAndShow(activity: Activity): Boolean {
            if (SdlSettings.isGamepadInputModePrompted()) {
                return false
            }
            val existing = sInstance?.get()
            if (existing != null && existing.isShowing) {
                return true
            }
            if (activity.isFinishing || activity.isDestroyed) {
                return false
            }
            val dialog = GamepadModePromptDialog(activity)
            sInstance = WeakReference(dialog)
            dialog.show()
            return true
        }
    }
}