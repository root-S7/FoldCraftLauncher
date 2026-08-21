package com.tungsten.fcllibrary.component.dialog

import android.content.Context
import androidx.appcompat.app.AppCompatDialog
import com.tungsten.fcl.R
import com.tungsten.fcllibrary.component.theme.ThemeEngine

open class FCLDialog @JvmOverloads constructor(context: Context, themeResId: Int = 0) : AppCompatDialog(context, themeResId) {
    init {
        ThemeEngine.getInstance()
            .applyFullscreen(window, ThemeEngine.getInstance().getTheme().fullscreen)
        window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }
}