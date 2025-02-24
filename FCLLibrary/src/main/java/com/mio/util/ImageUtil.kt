package com.mio.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.InputStream
import java.util.Optional

class ImageUtil {
    companion object {
        @JvmStatic
        fun getSize(path: String): Pair<Int, Int> {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            return Pair(options.outWidth, options.outHeight)
        }

        @JvmStatic
        fun getSize(inputStream: InputStream): Pair<Int, Int> {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            return Pair(options.outWidth, options.outHeight)
        }

        @JvmStatic
        fun getBitmapMemorySize(path: String): Long {
            val size = getSize(path)
            return size.first * size.second * 4L
        }

        @JvmStatic
        fun getBitmapMemorySize(inputStream: InputStream): Long {
            val size = getSize(inputStream)
            return size.first * size.second * 4L
        }

        @JvmStatic
        fun load(path: String): Optional<Bitmap> {
            if (!File(path).exists() or (getBitmapMemorySize(path) > 104857600)) {
                return Optional.empty<Bitmap>()
            }
            return Optional.ofNullable(BitmapFactory.decodeFile(path))
        }

        @JvmStatic
        fun load(inputStream: InputStream): Optional<Bitmap> {
            try {
                val memorySize = getBitmapMemorySize(inputStream)
                if (memorySize > 104857600) return Optional.empty<Bitmap>()

                inputStream.reset()

                val decodeStream = BitmapFactory.decodeStream(inputStream)

                return Optional.ofNullable(decodeStream)
            } catch (e: Exception) {
                return Optional.empty<Bitmap>()
            }
        }

    }
}