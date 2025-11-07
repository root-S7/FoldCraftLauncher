package com.root.model

import com.google.gson.annotations.SerializedName

class ThemeConfig {
    @SerializedName("theme_color")
    val themeColor: Int = 0

    @SerializedName("theme_color2")
    val themeColor2: Int = 0

    @SerializedName("theme_color2_dark")
    val themeColor2Dark: Int = 0

    @SerializedName("animation_speed")
    val animationSpeed: Byte = 7

    @SerializedName("close_skin_model")
    val closeSkinModel: Boolean = false

    @SerializedName("fullscreen")
    val fullscreen: Boolean = false
}