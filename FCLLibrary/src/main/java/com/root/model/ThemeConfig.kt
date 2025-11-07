package com.mio

import com.google.gson.annotations.SerializedName

data class ThemeConfig(
    @SerializedName("theme_color")
    val themeColor: Int = 0,

    @SerializedName("theme_color2")
    val themeColor2: Int = 0,

    @SerializedName("theme_color2_dark")
    val themeColor2Dark: Int = 0,

    @SerializedName("animation_speed")
    val animationSpeed: Int = 0,

    @SerializedName("close_skin_model")
    val closeSkinModel: Boolean = false,

    @SerializedName("fullscreen")
    val fullscreen: Boolean = false
)