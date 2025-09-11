package com.tungsten.fcl.util.check.rule

import android.graphics.BitmapFactory
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.tungsten.fcl.util.AndroidUtils.openAssets
import java.io.FileNotFoundException

object JsonRule : FileCheckRule {
    override fun check(assPath: String): Boolean {
        return try {
            val result = openAssets(null, assPath).use { input ->
                input.bufferedReader().use {
                    GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .fromJson(it, JsonElement::class.java)
                }
            }
            result ?: throw JsonParseException("")

            true
        }catch(ex: FileNotFoundException) {
            throw ex
        }catch(_: JsonParseException) {
            throw JsonParseException("文件『$assPath』解析错误，请检查是否为有效的Json格式！")
        }catch(_: Exception) {
            false
        }
    }
}

object ImageRule : FileCheckRule {
    private const val MAX_SIZE_BYTES = (1.11 * 1024 * 1024).toInt()
    private const val MAX_WIDTH = 2560
    private const val MAX_HEIGHT = 1440

    override fun check(assPath: String): Boolean = run {
        openAssets(null, assPath).use { input ->
            if(input.available() > MAX_SIZE_BYTES) throw IllegalArgumentException("『$assPath』图片大小超过1.11MB")

            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(input, null, opts)
            val w = opts.outWidth
            val h = opts.outHeight
            if(w <= 0 || h <= 0) throw IllegalArgumentException("文件『$assPath』不是有效的图片")
            if(w > MAX_WIDTH || h > MAX_HEIGHT) throw IllegalArgumentException("『$assPath』图片分辨率过大（最大 ${MAX_WIDTH}×${MAX_HEIGHT}）")

            true
        }
    }
}

object BaseRule : FileCheckRule {
    override fun check(assPath: String): Boolean {
        return try {
            openAssets(null, assPath).use { _ -> true }
        }catch(ex: FileNotFoundException) {
            throw ex
        }catch(_: Exception) {
            false
        }
    }
}