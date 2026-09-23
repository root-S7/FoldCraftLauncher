package com.tungsten.fcllibrary.component.dialog

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialog
import com.tungsten.fcl.R
import com.tungsten.fcllibrary.component.theme.ThemeEngine

open class FCLDialog : AppCompatDialog {

    constructor(context: Context) : super(context)

    constructor(context: Context, @StyleRes themeId: Int) : super(context, themeId)

    private fun applyFullscreen() {
        ThemeEngine.getInstance()
            .applyFullscreen(window, ThemeEngine.getInstance().getTheme().fullscreen)
    }

    init {
        applyFullscreen()
        window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    /**
     * 窗口失焦（返回主屏、切多任务）后系统会清除隐藏导航栏的沉浸标志，
     * 且此时持有焦点的是对话框窗口而非宿主 Activity，Activity 侧的重新应用不会触发，
     * 须在此重获焦点时自行恢复，否则导航栏保持显示并遮挡对话框底部
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyFullscreen()
        }
    }
}